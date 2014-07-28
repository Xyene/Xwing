package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.w3c.dom.*;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class XGenerator {
    public static final Map<String, Class> EXTERNS = new HashMap<String, Class>() {{
        put("menubar", JMenuBar.class);
        put("menu", JMenu.class);
        put("checkboxmenuitem", JCheckBoxMenuItem.class);
        put("radiomenuitem", JCheckBoxMenuItem.class);
        put("menuitem", JMenuItem.class);

        put("img", XWidget.Image.class);

        put("toolbar", JToolBar.class);

        put("label", JLabel.class);
        put("checkbox", JCheckBox.class);
        put("button", JButton.class);
        put("slider", XWidget.Slider.class);
        put("spinner", XWidget.Spinner.class);
        put("radiobutton", JRadioButton.class);
        put("textbox", JTextField.class);
        put("hiddentextbox", JPasswordField.class);
        put("textarea", JTextArea.class);
        put("panel", XWidget.Panel.class);
        put("window", XWidget.Frame.class);
        put("dialog", XWidget.Dialog.class);
        put("tabbedpanel", XWidget.TabbedPane.class);
        put("buttongroup", XWidget.ButtonGroup.class);
        put("hr", XWidget.HRuler.class);
        put("vr", XWidget.VRuler.class);
        put("splitpane", XWidget.SplitPane.class);
        put("scrollpane", XWidget.ScrollPane.class);
        put("progressbar", XWidget.ProgressBar.class);

        put("colorchooser", XWidget.ColorChooser.class);
        put("filechooser", XWidget.FileChooser.class);

        put("combobox", XWidget.ComboBox.class);
        put("choice", XWidget.ComboBox.Choice.class);

        put("grid", XWidget.Grid.class);

        put("hbox", XWidget.HBox.class);
        put("vbox", XWidget.VBox.class);
        put("box", XWidget.Box.class);
    }};

    private static void apply(Map<String, String> attributes, Container to) {
        {
            String width = attributes.get("width");
            String height = attributes.get("height");
            if (width != null || height != null) {
                Dimension dim = new Dimension(
                        width != null ? Integer.parseInt(width) : to.getPreferredSize().width,
                        height != null ? Integer.parseInt(height) : to.getPreferredSize().height);
                to.setPreferredSize(dim);
                to.setSize(dim);
            }
        }
        for (Map.Entry<String, String> pair : attributes.entrySet()) {
            String k = pair.getKey();
            String v = pair.getValue();
            switch (k) {
                case "width":
                case "height":
                    break;
                default:
                    // Delegate to setters
                    // TODO: for example, if setter argument is Dimension and given data is "50, 50", convert to Dimension(50, 50)
                    String handle = "set" + Character.toTitleCase(k.charAt(0)) + k.substring(1);

                    for (Method raw : to.getClass().getMethods()) {
                        if (raw.getName().equals(handle) && raw.getParameterTypes().length == 1) {
                            try {
                                Class type = raw.getParameterTypes()[0];
                                Object converted = v; // String.class
                                if (type == int.class)
                                    converted = Integer.parseInt(v);
                                else if (type == boolean.class)
                                    converted = Boolean.parseBoolean(v);
                                raw.invoke(to, converted);
                            } catch (Exception e) {
                                // This is perfectly normal for overloaded methods
                            }
                        }
                    }
            }
        }
    }

    private static Map<String, String> mapAttributes(Node node) {
        HashMap<String, String> map = new HashMap<>();
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null)
            return map;

        for (int i = 0; i != attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            map.put(attr.getNodeName(), attr.getNodeValue());
        }
        return map;
    }

    private static void build(Node node, Map<String, Component> byId, Stack<Container> hierarchy) {
        NodeList children = node.getChildNodes();

        for (int _ = 0; _ != children.getLength(); _++) {
            Node child = children.item(_);

            Map<String, String> attrs = mapAttributes(child);

            Class<Container> extern = EXTERNS.get(child.getNodeName());
            if (extern != null)
                try {
                    Constructor<Container> make = extern.getConstructor();
                    make.setAccessible(true);
                    Container instance = extern.newInstance();
                    apply(attrs, instance);
                    String _id = attrs.get("id");
                    if (_id != null) {
                        byId.put(_id, instance);
                    }

                    if (!hierarchy.empty()) {
                        Container comp = hierarchy.peek();
                        if (comp instanceof XWidget)
                            ((XWidget) comp).add(instance, attrs);
                        else
                            comp.add(instance);
                    }
                    hierarchy.push(instance);

                    build(child, byId, hierarchy);

                    hierarchy.pop();
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
        }
    }

    public static void bind(File xul, Container form, XJS js, Map<String, Component> byId) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xul);

        Stack<Container> hierarchy = new Stack<>();
        hierarchy.push(form);

        Node root = doc.getFirstChild();
        String rootName = root.getNodeName();

        apply(mapAttributes(root), form);

        if (!EXTERNS.get(rootName).isInstance(form))
            throw new IllegalArgumentException("form is not a " + rootName);

        build(root, byId, hierarchy);

        NodeList scripts = doc.getElementsByTagName("script");
        for (int id = 0; id != scripts.getLength(); id++) {
            Node node = scripts.item(id);
            String name = String.format("JS-Script-%s!%s", id, xul.getName());
            try {
                Context.enter();
                js.getContext().evaluateString(js.getScope(), node.getTextContent().trim(), name, 0, null);
                Context.exit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

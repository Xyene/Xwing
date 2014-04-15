package tk.ivybits.xwing;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sun.org.mozilla.javascript.internal.Context;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class XGenerator {
    public static void bind(final File xul, final XUI form) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            HashMap<String, Class> externs = new HashMap<String, Class>() {{
                put("label", JLabel.class);
                put("button", JButton.class);
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

                put("combobox", XWidget.ComboBox.class);
                put("choice", XWidget.ComboBox.Choice.class);

                put("progressbar", XWidget.ProgressBar.class);

                // BoxLayout externs
                put("hbox", XWidget.HBox.class);
                put("vbox", XWidget.VBox.class);
                put("box", XWidget.Box.class);
            }};
            Stack<Container> hierarchy = new Stack<>();
            boolean inScript = false;
            XScript currentScript = null;
            List<XScript> scripts = new ArrayList<>();

            public Map<String, String> mapAttributes(Attributes attrs) {
                HashMap<String, String> map = new HashMap<>();
                for (int idx = 0; idx != attrs.getLength(); idx++)
                    map.put(attrs.getLocalName(idx), attrs.getValue(idx));
                return map;
            }

            public void apply(Map<String, String> attributes, Container to) {
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
                        case "id":
                            form.byId.put(v, to);
                        default:
                            // Delegate to setters
                            // TODO: for example, if setter argument is Dimension and given data is "50, 50", convert to Dimension
                            String handle = "set" + Character.toTitleCase(k.charAt(0)) + k.substring(1);

                            for (Method raw : to.getClass().getMethods()) {
                                if (raw.getName().equals(handle) && raw.getParameterTypes().length == 1) {
                                    try {
                                        Class type = raw.getParameterTypes()[0];
                                        Object converted = v; // String.class
                                        if (type == int.class)
                                            converted = Integer.parseInt(v);
                                        if (type == boolean.class)
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

            @Override
            public void startElement(String uri, String localName, String qName,
                                     Attributes attributes) throws SAXException {
                Map<String, String> attrs = mapAttributes(attributes);
                switch (qName) {
                    case "script":
                        inScript = true;
                        currentScript = new XScript();
                        String id = attrs.get("id");
                        if (id == null) {
                            id = "JS-Script-" + (scripts.size() + 1) + "!" + xul.getName();
                        }
                        currentScript.setId(id);
                        break;
                    default:
                        Class<Container> extern = externs.get(qName);
                        if (extern != null)
                            try {
                                Constructor<Container> make = extern.getConstructor();
                                make.setAccessible(true);
                                Container instance = extern.newInstance();
                                apply(attrs, instance);

                                if (!hierarchy.empty()) {
                                    Container comp = hierarchy.peek();
                                    if (comp instanceof XWidget)
                                        ((XWidget) comp).add(instance, attrs);
                                    else
                                        comp.add(instance);
                                }
                                hierarchy.push(instance);
                            } catch (ReflectiveOperationException e) {
                                e.printStackTrace();
                            }

                }
            }

            @Override
            public void endElement(String uri, String localName,
                                   String qName) throws SAXException {
                switch (qName) {
                    case "script":
                        inScript = false;
                        scripts.add(currentScript);
                        currentScript = null;
                        break;
                    default:
                        if (externs.containsKey(qName))
                            hierarchy.pop();
                        break;
                }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                if (inScript) {
                    currentScript.setScript(currentScript.getScript() + new String(ch, start, length));
                }
            }

            @Override
            public void endDocument() {
                for (final XScript script : scripts)
                    try {
                        Context.enter();
                        form.context.evaluateString(form.scope, script.getScript(), script.getId(), 1, null);
                        Context.exit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        };

        saxParser.parse(xul, handler);
    }
}

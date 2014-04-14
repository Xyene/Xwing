package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.mozilla.javascript.ScriptableObject;

import javax.script.ScriptException;
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
    public static void bind(File xul, final XForm form) throws Exception {
        bindCrossovers(form);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            HashMap<String, Class> externs = new HashMap<String, Class>() {{
                put("label", JLabel.class);
                put("button", JButton.class);
                put("radiobutton", JRadioButton.class);
                put("textbox", JTextField.class);
                put("textarea", JTextArea.class);
                put("panel", XWidgets.Panel.class);
                put("window", XWidgets.Frame.class);
                put("tabbedpanel", XWidgets.TabbedPane.class);
                put("hbox", XWidgets.HBox.class);
                put("vbox", XWidgets.VBox.class);
                put("buttongroup", XWidgets.ButtonGroup.class);
            }};
            Stack<Container> hierarchy = new Stack<>();
            boolean inScript = false;
            String currentScript = "";
            List<String> scripts = new ArrayList<>();

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
                        if (width == null) width = "0";
                        if (height == null) height = "0";
                        Dimension dim = new Dimension(Integer.parseInt(width), Integer.parseInt(height));
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
                            String handle = "set" + Character.toTitleCase(k.charAt(0)) + k.substring(1);
                            try {
                                for (Method raw : to.getClass().getMethods()) {
                                    if (raw.getName().equals(handle) && raw.getParameterTypes().length == 1) {
                                        Class type = raw.getParameterTypes()[0];
                                        Object converted = v; // String.class
                                        if (type == int.class)
                                            converted = Integer.parseInt(v);
                                        if (type == boolean.class)
                                            converted = Boolean.parseBoolean(v);
                                        raw.invoke(to, converted);
                                    }
                                }
                            } catch (ReflectiveOperationException e) {
                                e.printStackTrace();
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
                        break;
                    default:
                        Class<Container> extern = externs.get(qName);
                        if (extern != null)
                            try {
                                Constructor<Container> make = extern.getConstructor();
                                make.setAccessible(true);
                                Container instance = extern.newInstance();
                                apply(attrs, instance);
                                if (!hierarchy.empty())
                                    hierarchy.peek().add(instance);
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
                        currentScript = "";
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
                    currentScript += new String(ch, start, length);
                }
            }

            @Override
            public void endDocument() {
                for (String script : scripts)
                    try {
                        Context.enter();
                        form.context.evaluateString(form.scope, script, "<cmd>", 1, null);
                        Context.exit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        };

        saxParser.parse(xul, handler);
    }

    private static void bindCrossovers(XForm form) throws ScriptException {
        ScriptableObject.putProperty(form.scope, "xform", form);
        // http://stackoverflow.com/a/2554033
        Context.enter();
        form.context.evaluateString(form.scope, "for(var fn in xform) {\n" +
                "  if(typeof xform[fn] === 'function') {\n" +
                "    this[fn == '$' ? fn : '$' + fn] = (function() {\n" +
                "      var method = xform[fn];\n" +
                "      return function() {\n" +
                "         return method.apply(xform, arguments);\n" +
                "      };\n" +
                "    })();\n" +
                "  }\n" +
                "}", "<cmd>", 1, null);
        Context.exit();
    }
}

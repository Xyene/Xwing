package tk.ivybits.xwing;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class XFrame extends XWidget.Frame implements XContainer {
    private final XJS js;
    private final Map<String, Component> byId = new HashMap<>();

    public XFrame(InputStream binding, Object controller) {
        js = new XJS(this, controller);
        try {
            XGenerator.bind(binding, this, js, byId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Component byId(String name) {
        return byId.get(name);
    }
}

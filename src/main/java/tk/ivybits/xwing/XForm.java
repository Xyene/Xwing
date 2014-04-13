package tk.ivybits.xwing;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

public class XForm {
    HashMap<String, Component> byId = new HashMap<>();
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine js = mgr.getEngineByName("JavaScript");

    protected XForm bind(File ui) {
        try {
            XGenerator.bind(ui, this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public <T extends Component> ProxiedContainer<T> $(String id) {
        return new ProxiedContainer<T>(this, (T) byId.get(id));
    }
}

package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

public class XForm {
    HashMap<String, Component> byId = new HashMap<>();
    Context context;
    ScriptableObject scope;

    protected XForm bind(File ui) {
        try {
            context = Context.enter();
            scope = context.initStandardObjects();
            Context.exit();
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

package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

public class XUI {
    HashMap<String, Component> byId = new HashMap<>();
    Context context;
    ScriptableObject scope;

    public XUI(final File ui) {
        try {
            context = Context.enter();
            scope = context.initStandardObjects();
            // Add all methods of our form to global scope
            ScriptableObject.putProperty(scope, "xform", XUI.this);
            context.evaluateString(scope,
                    "for(var fn in xform) {" +
                            "    if(typeof xform[fn] === 'function') {" +
                            "      this[fn] = (function() {" +
                            "        var method = xform[fn];" +
                            "        return function() {" +
                            "           return method.apply(xform, arguments);" +
                            "        };" +
                            "      })();" +
                            "    }" +
                            "}" +
                            "this['print'] = function (s) {" +
                            "    Packages.java.lang.System['out'].println(s);" +
                            "};", "<cmd>", 1, null
            );
            XGenerator.bind(ui, XUI.this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Component> ProxiedContainer<T> $(String id) {
        return new ProxiedContainer<>(this, (T) byId.get(id));
    }
}

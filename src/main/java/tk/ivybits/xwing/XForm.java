package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class XForm {
    HashMap<String, Component> byId = new HashMap<>();
    Context context;
    ScriptableObject scope;

    public XForm(final File ui) {
        try {
            context = Context.enter();
            System.out.println(context);
            scope = context.initStandardObjects();
            ScriptableObject.putProperty(scope, "xform", XForm.this);
            context.evaluateString(scope,
                    "for(var fn in xform) {" +
                            "    if(typeof xform[fn] === 'function') {" +
                            "      this[fn == '$' ? fn : '$' + fn] = (function() {" +
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
            XGenerator.bind(ui, XForm.this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Component> ProxiedContainer<T> $(String id) {
        return new ProxiedContainer<T>(this, (T) byId.get(id));
    }
}

package tk.ivybits.xwing;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class XForm {
    HashMap<String, Component> byId = new HashMap<>();
    Context context;
    ScriptableObject scope;
    Thread jsThread;
    LinkedBlockingQueue<Runnable> jsTasks = new LinkedBlockingQueue<Runnable>();

    public XForm(final File ui) {
        try {
            final Semaphore semaphore = new Semaphore(1);
            semaphore.acquire();
            jsThread = new Thread() {
                public void run() {
                    context = Context.enter();
                    scope = context.initStandardObjects();
                    ScriptableObject.putProperty(scope, "xform", XForm.this);
                    context.evaluateString(scope, "for(var fn in xform) {" +
                            "  if(typeof xform[fn] === 'function') {" +
                            "    this[fn == '$' ? fn : '$' + fn] = (function() {" +
                            "      var method = xform[fn];" +
                            "      return function() {" +
                            "         return method.apply(xform, arguments);" +
                            "      };" +
                            "    })();" +
                            "  }" +
                            "}" +
                            "this['print']=function (s) {Packages.java.lang.System['out'].println(s);};", "<cmd>", 1, null);

                    try {
                        XGenerator.bind(ui, XForm.this);
                        semaphore.release();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    while (true) {
                        try {
                            Runnable task = jsTasks.take();
                            task.run();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            jsThread.start();
            semaphore.acquire();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Component> ProxiedContainer<T> $(String id) {
        return new ProxiedContainer<T>(this, (T) byId.get(id));
    }
}

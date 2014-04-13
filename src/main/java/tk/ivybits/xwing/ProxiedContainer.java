package tk.ivybits.xwing;

import org.mozilla.javascript.*;
import sun.org.mozilla.javascript.internal.annotations.JSFunction;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ProxiedContainer<T extends Component> extends ScriptableObject {
    private final XForm form;
    private final Component component;
    HashMap<String, Method> proxied;

    public ProxiedContainer(XForm form, T component) {
        this.form = form;
        this.component = component;
        proxied = new HashMap<>();
        for (Method method : component.getClass().getMethods()) {
            String name = method.getName();
//            if (name.startsWith("is")) {
//                proxied.put(name.substring(2), method);
//            } else if (name.startsWith("get")) {
//                proxied.put(name.substring(3), method);
//            } else if (name.startsWith("set")) {
//                proxied.put(name.substring(3), method);
//            }
            proxied.put(name, method);
        }
        System.out.println(proxied);
    }

    @JSFunction
    public void onClick(final Object call) {
        System.out.println(call + ": " + component);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (call instanceof Function) {
                    Function func = (Function) call;
                    Context.enter();
                    func.call(form.context, form.scope, form.scope, new Object[]{mouseEvent});
                    Context.exit();
                }
            }
        });
    }

    @Override
    public Object get(String name, Scriptable start) {
        switch (name) {
            case "onClick":
                return new BaseFunction() {
                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                       Object[] args) {
                        onClick(args[0]);
                        return null;
                    }
                };
            default:
                final Method proxy = proxied.get(name);
                if (proxy != null) {
                    return new BaseFunction() {
                        @Override
                        public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                           Object[] args) {
                            try {
                                return proxy.invoke(component, args);
                            } catch (ReflectiveOperationException e) {
                                e.printStackTrace();

                                return null;
                            }
                        }
                    };

                }
                return super.get(name, start);
        }
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return toString();
    }

    public <T> T get() {
        return (T) component;
    }

    @Override
    public String getClassName() {
        return component.getClass().getName();
    }
}

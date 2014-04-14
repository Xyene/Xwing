package tk.ivybits.xwing;

import org.mozilla.javascript.*;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

public class ProxiedContainer<T extends Component> extends ScriptableObject {
    private final XUI form;
    private final Component component;

    public ProxiedContainer(XUI form, T component) {
        this.form = form;
        this.component = component;
    }

    public void onClick(final Object call) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent mouseEvent) {
                if (call instanceof Function) {
                    Context.enter();
                    ((Function) call).call(form.context, form.scope, form.scope, new Object[]{mouseEvent});
                    Context.exit();
                }
            }
        });
    }

    public void onPress(final Object call) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent mouseEvent) {
                if (call instanceof Function) {
                    Context.enter();
                    ((Function) call).call(form.context, form.scope, form.scope, new Object[]{mouseEvent});
                    Context.exit();
                }
            }
        });
    }

    public void onRelease(final Object call) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(final MouseEvent mouseEvent) {
                if (call instanceof Function) {
                    Context.enter();
                    ((Function) call).call(form.context, form.scope, form.scope, new Object[]{mouseEvent});
                    Context.exit();
                }
            }
        });
    }

    public void toCenter() {
        // setLocationRelativeTo(null) does not work on Ubuntu 12.04 for centering window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        component.setLocation(new Point((screenSize.width / 2) - (component.getWidth() / 2), screenSize.height / 2 - (component.getHeight() / 2)));
    }

    @Override
    public Object get(final String name, Scriptable start) {
        switch (name) {
            case "onClick":
                return new BaseFunction() {
                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                       Object[] args) {
                        onClick(args[0]);
                        return thisObj;
                    }
                };
            case "onPress":
                return new BaseFunction() {
                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                       Object[] args) {
                        onPress(args[0]);
                        return thisObj;
                    }
                };
            case "onRelease":
                return new BaseFunction() {
                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                       Object[] args) {
                        onRelease(args[0]);
                        return thisObj;
                    }
                };
            case "toCenter":
                if (component instanceof Window)
                    return new BaseFunction() {
                        @Override
                        public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                           Object[] args) {
                            toCenter();
                            return thisObj;
                        }
                    };
                return null;
            default:
                return new BaseFunction() {
                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                       Object[] args) {
                        for (int idx = 0; idx != args.length; idx++) {
                            // If one of the arguments is a ProxiedContainer, replace it with the proxied object
                            // On the JS side, this means being able to do things like
                            // $("...").setVisible(...)
                            // instead of
                            // $("...").get().setVisible(...)
                            if (args[idx] instanceof ProxiedContainer) {
                                args[idx] = ((ProxiedContainer) args[idx]).get();
                            }
                        }
                        try {
                            Class[] types = new Class[args.length];
                            for (int idx = 0; idx != types.length; idx++) {
                                Class clazz = args[idx] != null ? args[idx].getClass() : Object.class;
                                // All Swing methods take primitives, however Rhino passes us the object primitives
                                // Here we convert in a horrible manner object primitive classes to primitves
                                // so that the reflection below may work
                                if (clazz == Long.class)
                                    clazz = long.class;
                                else if (clazz == Integer.class)
                                    clazz = int.class;
                                else if (clazz == Short.class)
                                    clazz = short.class;
                                else if (clazz == Character.class)
                                    clazz = char.class;
                                else if (clazz == Byte.class)
                                    clazz = byte.class;
                                else if (clazz == Double.class)
                                    clazz = double.class;
                                else if (clazz == Float.class)
                                    clazz = float.class;
                                else if (clazz == Boolean.class)
                                    clazz = boolean.class;

                                types[idx] = clazz;
                            }
                            _outer:
                            // Find a method matching the description
                            for (Method method : component.getClass().getMethods()) {
                                if (method.getName().equals(name)) {
                                    Class[] params = method.getParameterTypes();
                                    if (params.length == types.length) {
                                        for (int idx = 0; idx != params.length; idx++) {
                                            if (!(types[idx] == params[idx] || params[idx].isAssignableFrom(params[idx]))) {
                                                continue _outer;
                                            }
                                        }
                                        // Found, invoke it
                                        Object ret = method.invoke(component, args);
                                        // If it returns void return self; allows fluent interface in JS part
                                        if(method.getReturnType() == void.class)
                                            return thisObj;
                                        return ret;
                                    }
                                }
                            }
                            return null;
                        } catch (ReflectiveOperationException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                };
        }
    }

    public <T> T get() {
        return (T) component;
    }

    @Override
    public Object getDefaultValue(Class<?> typeHint) {
        return toString();
    }

    @Override
    public String getClassName() {
        return component.getClass().getName();
    }
}

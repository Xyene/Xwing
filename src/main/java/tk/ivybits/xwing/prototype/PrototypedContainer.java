package tk.ivybits.xwing.prototype;

import org.mozilla.javascript.*;
import tk.ivybits.xwing.XJS;

import java.awt.*;
import java.lang.reflect.Method;

public class PrototypedContainer<T> extends ScriptableObject {
    private final XJS form;
    private final T component;

    public PrototypedContainer(XJS form, T component) {
        if (component == null) throw new IllegalArgumentException("component must not be null");
        this.form = form;
        this.component = component;
    }

    public PrototypedContainer[] getComponents() {
        Container c = (Container) component;
        PrototypedContainer[] comps = new PrototypedContainer[c.getComponentCount()];
        for (int i = 0; i != comps.length; i++)
            comps[i] = new PrototypedContainer(form, c.getComponent(i));
        return comps;
    }

    @Override
    public Object get(final String name, Scriptable start) {
        switch (name) {
            case "getComponents":
                if (component instanceof Container) {
                    return new BaseFunction() {
                        @Override
                        public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                           Object[] args) {
                            return getComponents();
                        }
                    };
                }
                return null;
            default:
                return new BaseFunction() {
                    @Override
                    public Object call(Context cx, Scriptable scope, Scriptable thisObj,
                                       Object[] args) {
                        for (int idx = 0; idx != args.length; idx++) {
                            final Object arg = args[idx];
                            // If one of the arguments is a PrototypedContainer, replace it with the proxied object
                            // On the JS side, this means being able to do things like
                            // $("...").setVisible(...)
                            // instead of
                            // $("...").get().setVisible(...)
                            if (arg instanceof PrototypedContainer) {
                                args[idx] = ((PrototypedContainer) arg).get();
                            } else if (arg instanceof Function) {
                                args[idx] = new Prototype.Callback() {
                                    @Override
                                    public void run(Object... args) {
                                        Context.enter();
                                        ((Function) arg).call(form.getContext(), form.getScope(), form.getScope(), args);
                                        Context.exit();
                                    }
                                };
                            }
                        }

                        if (XJS.PROTOTYPES.get(name) != null) {
                            Prototype pt = XJS.PROTOTYPES.get(name);
                            if (pt.isApplicableOn(component)) {
                                pt.run(component, args.getClass().isArray() ? args : new Object[]{args});
                                return thisObj;
                            }
                        }

                        try {
                            Class[] types = new Class[args.length];
                            for (int idx = 0; idx != types.length; idx++) {
                                Class clazz = args[idx] != null ? args[idx].getClass() : Object.class;
                                // All Swing methods take primitives, however Rhino passes us the object primitives
                                clazz = Reflection.toPrimitive(clazz);
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
                                        if (method.getReturnType() == void.class)
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

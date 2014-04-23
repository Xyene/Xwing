package tk.ivybits.xwing.prototype;

import tk.ivybits.xwing.XJS;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DynamicPrototype {
    public static void mapAll(Class listener) {
        Method[] methods = listener.getMethods();
        String delegate = "add" + listener.getSimpleName();
        for (Method method : methods) {
            String name = method.getName();
            name = "on" + Character.toTitleCase(name.charAt(0)) + name.substring(1);
            XJS.PROTOTYPES.put(name, makeListener(delegate, method.getName(), listener));
        }
    }

    public static Prototype makeListener(final String boundName, final String override, final Class... args) {
        return new Prototype() {
            @Override
            public Object run(Object component, final Object... callback) {
                InvocationHandler handler = new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals(override)) {
                            ((Callback) callback[0]).run(callback);
                        }
                        return null;
                    }
                };
                Object instance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), args, handler);
                Reflection.method(boundName).withParameterTypes(args).in(component).invoke(instance);
                return null;
            }

            @Override
            public boolean isApplicableOn(Object component) {
                return Reflection.method(boundName).withParameterTypes(args).in(component).exists();
            }
        };
    }
}

package tk.ivybits.xwing.prototype;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * A helper class to make reflection more bearable.
 */
public class Reflection {

    public static Class toPrimitive(Class clazz) {
        if (clazz == Long.class)
            return long.class;
        else if (clazz == Integer.class)
            return int.class;
        else if (clazz == Short.class)
            return short.class;
        else if (clazz == Character.class)
            return char.class;
        else if (clazz == Byte.class)
            return byte.class;
        else if (clazz == Double.class)
            return double.class;
        else if (clazz == Float.class)
            return float.class;
        else if (clazz == Boolean.class)
            return boolean.class;
        return clazz;
    }

    public static <T> FieldContainer<T> declaredField(String name) {
        return new FieldContainer<>(name, true);
    }

    public static <T> FieldContainer<T> field(String name) {
        return new FieldContainer<>(name, false);
    }

    public static <T> MethodContainer<T> declaredMethod(String name) {
        return new MethodContainer<>(name, true);
    }

    public static <T> MethodContainer<T> method(String name) {
        return new MethodContainer<>(name, false);
    }

    public static <T> ConstructorContainer<T> constructor() {
        return new ConstructorContainer<>();
    }

    public static class MethodContainer<T> extends Container {
        // <Class, <Method Name, <Signature, Method>>
        private static final Map<Class, Map<String, List<Method>>> lookup = new HashMap<>();
        private Class<?>[] param;
        private boolean declared;
        private Method raw;

        public MethodContainer(String name, boolean declared) {
            super(name);
            this.declared = declared;
        }

        public <T> MethodContainer<T> withReturnType(Class<T> clazz) {
            return (MethodContainer<T>) this;
        }

        public MethodContainer<T> in(Object clazz) {
            if (!(clazz instanceof Class)) {
                in = clazz;
                target = clazz.getClass();
            } else {
                target = (Class<?>) clazz;
            }
            return this;
        }

        public MethodContainer<T> withParameterTypes(Class<?>... args) {
            param = args;
            return this;
        }

        public T invoke(Object... args) {
            try {
                Method raw = getRaw();
                boolean accessible = raw.isAccessible();
                raw.setAccessible(true);
                T ret = (T) raw.invoke(in, args);
                raw.setAccessible(accessible);
                return ret;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean exists() {
            return getRaw() != null;
        }

        public Method getRaw() {
            if (raw != null)
                return raw;
            Map<String, List<Method>> methods = lookup.get(target);
            Method raw = null;
            if (methods != null) {
                List<Method> get = methods.get(name);
                if (get != null)
                    for (int i = 0; i < get.size(); i++) {
                        Method m = get.get(i);
                        Class<?>[] args = m.getParameterTypes();
                        boolean noParams = param == null;
                        if ((args.length == 0 && noParams) || (!noParams && Arrays.equals(args, param))) {
                            raw = m;
                            break;
                        }
                    }
            }
            if (raw == null) {
                try {
                    if (param != null && param.length > 0) {
                        raw = declared ? target.getDeclaredMethod(name, param) : target.getMethod(name, param);
                    } else {
                        raw = declared ? target.getDeclaredMethod(name) : target.getMethod(name);
                    }
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    return null;
                }

                Map<String, List<Method>> cache = methods;
                if (methods == null) {
                    lookup.put(target, cache = new HashMap<>());
                }
                List<Method> ms = cache.get(name);
                if (ms == null) {
                    cache.put(name, ms = new ArrayList<>());
                }
                ms.add(raw);
                this.raw = raw;
            }
            return raw;
        }
    }

    public static class ConstructorContainer<T> extends Container {
        private Class<?>[] param;

        public ConstructorContainer<T> withParameters(Class<?>... args) {
            param = args;
            return this;
        }

        public <T> ConstructorContainer<T> in(Class<T> clazz) {
            return (ConstructorContainer<T>) this;
        }

        public T newInstance(Object... args) {
            try {
                Constructor raw = getRaw();
                boolean accessible = raw.isAccessible();
                raw.setAccessible(true);
                T instance = (T) raw.newInstance(args);
                raw.setAccessible(accessible);
                return instance;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean exists() {
            return getRaw() != null;
        }

        public Constructor<?> getRaw() {
            try {
                Constructor<?> raw = (param != null && param.length > 0) ? target.getDeclaredConstructor(param) : target.getDeclaredConstructor();
                return raw;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static class FieldContainer<T> extends Container {
        protected boolean declared;

        public FieldContainer(String name, boolean declared) {
            super(name);
            this.declared = declared;
        }

        public <T> FieldContainer<T> ofType(Class<T> clazz) {
            return (FieldContainer<T>) this;
        }

        public FieldContainer<T> in(Object clazz) {
            if (!(clazz instanceof Class)) {
                in = clazz;
                target = clazz.getClass();
            } else {
                target = (Class<?>) clazz;
            }
            return this;
        }

        public void set(Object object) {
            try {
                Field raw = getRaw();
                boolean accessible = raw.isAccessible();
                raw.setAccessible(true);
                raw.set(in, object);
                raw.setAccessible(accessible);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public T get() {
            try {
                Field raw = getRaw();
                boolean accessible = raw.isAccessible();
                raw.setAccessible(true);
                T ret = (T) raw.get(in);
                raw.setAccessible(accessible);
                return ret;
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean exists() {
            return getRaw() != null;
        }

        public Field getRaw() {
            try {
                return declared ? target.getDeclaredField(name) : target.getField(name);
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }
    }

    public static class Container {
        protected String name;
        protected Object in;
        protected Class<?> target;

        public Container(String name) {
            this.name = name;
        }

        public Container() {
        }
    }
}
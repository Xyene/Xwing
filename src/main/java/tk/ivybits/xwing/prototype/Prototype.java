package tk.ivybits.xwing.prototype;

public interface Prototype<T> {
    Object run(T t, Object... args);

    boolean isApplicableOn(T t);

    public interface Callback {
        public void run(Object... args);
    }
}

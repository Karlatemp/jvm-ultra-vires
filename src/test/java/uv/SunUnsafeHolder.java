package uv;

import sun.misc.Unsafe;

public class SunUnsafeHolder {
    public static final Unsafe unsafe;

    static {
        try {
            // jdk.unsupported opens `sun.misc` for all with any vm options.
            var field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

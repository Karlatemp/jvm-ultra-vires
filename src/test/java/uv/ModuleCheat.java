package uv;

import io.github.karlatemp.unsafeaccessor.Unsafe;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ModuleCheat {
    @Test
    void cheat() throws Throwable {
        var usf = Unsafe.getUnsafe();
        var offset = usf.objectFieldOffset(Class.class, "module");

        /*
        Can only cheat `java.lang.reflect`, can't cheat jvm native
         */
        usf.putReference(getClass(), offset, Object.class.getModule());
        Assertions.assertSame(getClass().getModule(), Object.class.getModule());

        /*
        ``` // AccessibleObject.checkCanSetAccessible()
        if (callerModule == Object.class.getModule()) return true;
        ```
         */
        var javaInstrumentation = Class.forName("java.lang.instrument.Instrumentation");
        javaInstrumentation.getMethod("getAllLoadedClasses").setAccessible(true);
    }
}

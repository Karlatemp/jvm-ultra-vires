package uv;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.AccessibleObject;

@SuppressWarnings({"deprecation"})
@DisplayName("Inject by `sun.misc.Unsafe`")
public class BySunUnsafe {
    @Test
    @DisplayName("find offset of `java.lang.reflect.AccessibleObject`")
    void findAccessibleObjectOffset() throws Throwable {
        class NestedAccessibleObject extends AccessibleObject {
            boolean f;
        }
        var usf = SunUnsafeHolder.unsafe;

        var endOffset = usf.objectFieldOffset(NestedAccessibleObject.class.getDeclaredField("f"));
        var objRam = new boolean[(int) endOffset];
        var accessibleObject = new NestedAccessibleObject();

        accessibleObject.setAccessible(false);

        for (var i = 0; i < endOffset; i++) {
            objRam[i] = usf.getBoolean(accessibleObject, i);
        }

        // Compare diff
        long overrideOffset = -1;
        accessibleObject.setAccessible(true);
        for (var i = 0; i < endOffset; i++) {
            var tmp = usf.getBoolean(accessibleObject, i);
            if (tmp && !objRam[i]) { // tmp == true && objRam[i] != tmp
                overrideOffset = i;
            }
        }

        // assert
        usf.putBoolean(accessibleObject, overrideOffset, false);
        Assertions.assertFalse(accessibleObject.isAccessible());

    }
}

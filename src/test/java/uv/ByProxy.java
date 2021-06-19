package uv;

import jdk.internal.access.SharedSecrets;
import jdk.internal.misc.Unsafe;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes", "ConstantConditions"})
@DisplayName("Inject by `java.lang.Proxy`")
public class ByProxy {
    static class NestedClass {
        static {
            run();
        }

        static void run() {
            var jla = SharedSecrets.getJavaLangAccess();
            jla.addExports(
                    Object.class.getModule(),
                    "jdk.internal.misc",
                    NestedClass.class.getModule()
            );

            var usf = Unsafe.getUnsafe();
            System.out.println("Usf = " + usf);

            var ccl = NestedClass.class.getClassLoader();

            var module = (Module) ((Supplier) ccl).get();
            System.out.println("To open: " + module);
            if (false) {
                jla.addOpens(
                        Object.class.getModule(),
                        "jdk.internal.misc",
                        module
                );
            }
        }
    }

    @Test
    @DisplayName("inject by `java.lang.reflect.Proxy`")
    @SuppressWarnings("SuspiciousInvocationHandlerImplementation")
    void byProxy() throws Throwable {
        var classLoader = new BytecodeUtil.CLoader();
        classLoader.env = getClass().getModule();

        var code = ByProxy.class.getResourceAsStream("ByProxy$NestedClass.class").readAllBytes();

        var proxy = Proxy.newProxyInstance(classLoader, new Class[]{
                Class.forName("jdk.internal.access.JavaLangAccess")
        }, (proxy1, method, args) -> null).getClass();

        Assertions.assertTrue(Object.class.getModule().isExported(
                "jdk.internal.access", proxy.getModule()
        ));

        var remappedName = proxy.getPackageName() + ".PMInjector" + UUID.randomUUID();
        code = BytecodeUtil.replaceClassName(code, ByProxy.class.getName() + "$NestedClass", remappedName);

        var injector = classLoader.load(code);
        System.out.println(injector);
        Class.forName(injector.getName(), true, classLoader); // call `static {}`
    }
}

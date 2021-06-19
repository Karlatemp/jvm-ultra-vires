package uv;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Supplier;

class BytecodeUtil {

    static byte[] replace(byte[] source, byte[] replace, byte[] target) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length);
        int sourceLength = source.length,
                replaceLength = replace.length,
                targetLength = target.length,
                replaceLengthR1 = replaceLength - 1;
        root:
        for (int i = 0; i < sourceLength; i++) {
            if (i + replaceLength <= sourceLength) {
                for (int z = 0; z < replaceLength; z++) {
                    if (replace[z] != source[i + z]) {
                        outputStream.write(source[i]);
                        continue root;
                    }
                }
                outputStream.write(target, 0, targetLength);
                i += replaceLengthR1;
            } else {
                outputStream.write(source[i]);
            }
        }
        return outputStream.toByteArray();
    }

    static byte[] replace(byte[] classfile, String const1, String const2) {
        return replace(classfile, toJvm(const1), toJvm(const2));
    }

    static byte[] replaceClassName(byte[] classfile, String c1, String c2) {
        classfile = replace(classfile, c1, c2);
        if (c1.indexOf('/') == -1) {
            classfile = replace(classfile,
                    c1.replace('.', '/'),
                    c2.replace('.', '/')
            );
            classfile = replace(classfile,
                    "L" + c1.replace('.', '/') + ";",
                    "L" + c2.replace('.', '/') + ";"
            );
        } else {
            classfile = replace(classfile,
                    c1.replace('/', '.'),
                    c2.replace('/', '.')
            );
            classfile = replace(classfile,
                    "L" + c1 + ";",
                    "L" + c2 + ";"
            );
        }
        return classfile;
    }

    static byte[] toJvm(String const0) {
        byte[] bytes = const0.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length + 2);
        try {
            new DataOutputStream(bos).writeShort(bytes.length);
        } catch (IOException ioException) {
            throw new AssertionError(ioException);
        }
        bos.write(bytes, 0, bytes.length);
        return bos.toByteArray();
    }

    static class CLoader extends ClassLoader
            implements // envs api
            Supplier<Object>, Consumer<Object> {
        private static PermissionCollection pc() {
            var p = new AllPermission();
            var c = p.newPermissionCollection();
            c.add(p);
            c.setReadOnly();
            return c;
        }

        Object env;

        @Override
        public Object get() {
            return env;
        }

        @Override
        public void accept(Object o) {
            env = o;
        }

        ProtectionDomain domain = new ProtectionDomain(null, pc());

        Class<?> load(byte[] code) {
            return defineClass(null, code, 0, code.length, domain);
        }

        Class<?> load(String code) {
            return load(Base64.getDecoder().decode(code));
        }
    }
}

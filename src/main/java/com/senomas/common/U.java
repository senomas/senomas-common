package com.senomas.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.Key;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Node;

public abstract class U {
    protected static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final int MAX_DUMP_DEPTH = 10;
    public static SecretKey cryptKey;
    public static final Charset UTF8 = Charset.forName("UTF-8");

    static {
        byte[] iv = new byte[] {
                0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
        };
        for (int i = 0, il = iv.length; i < il; i++) {
            iv[i] = (byte) (iv[i] ^ 27);
        }
        try {
            PBEKeySpec password = new PBEKeySpec("agus@senomas.com---dodoldurenbakwan".toCharArray(), iv, 1000, 128);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKey pkey = (PBEKey) factory.generateSecret(password);
            cryptKey = new SecretKeySpec(pkey.getEncoded(), "AES");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public static <T> boolean contain(T[] arrays, T v) {
        for (T t : arrays) {
            if (t.equals(v)) return true;
        }
        return false;
    }

    public static byte[] getBytes(String str) {
        return str.getBytes(UTF8);
    }

    public static String toString(byte bb[]) {
        return new String(bb, UTF8);
    }

    public static String encode64(byte b[]) {
        return Base64.encodeBase64String(b);
    }

    public static String encode64(byte b[], int offset, int len) {
        if (offset == 0 && len == b.length) {
            return Base64.encodeBase64String(b);
        } else {
            byte bb[] = new byte[len];
            System.arraycopy(b, offset, bb, 0, len);
            return Base64.encodeBase64String(bb);
        }
    }

    public static byte[] decode64(String s) {
        return Base64.decodeBase64(s);
    }

    public static String toHex(byte b[]) {
        return toHex(b, 0, b.length);
    }

    public static String toHex(byte b[], int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset, il = Math.min(b.length, offset + length); i < il; i++) {
            // if (i != offset) sb.append(' ');
            int x = b[i];
            if (x < 0) x += 256;
            int d = (x / 16) & 0xF;
            sb.append((char) (d < 10 ? d + '0' : d + 'a' - 10));
            d = x & 0xF;
            sb.append((char) (d < 10 ? d + '0' : d + 'a' - 10));
        }
        return sb.toString();
    }

    public static String toHexUpperCase(byte b[], int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset, il = Math.min(b.length, offset + length); i < il; i++) {
            // if (i != offset) sb.append(' ');
            int x = b[i];
            if (x < 0) x += 256;
            int d = (x / 16) & 0xF;
            sb.append((char) (d < 10 ? d + '0' : d + 'A' - 10));
            d = x & 0xF;
            sb.append((char) (d < 10 ? d + '0' : d + 'A' - 10));
        }
        return sb.toString();
    }

    public static byte[] fromHex(String str) {
        int bl = 0;
        for (int i = 0, il = str.length(); i < il; i++) {
            char ch = str.charAt(i);
            if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')) {
                bl++;
            } else if (Character.isWhitespace(ch)) {
                // ignore
            } else {
                throw new RuntimeException("Invalid char '" + ch + "' at " + i + " [" + str + "]");
            }
        }
        if (bl % 2 == 1) throw new RuntimeException("Invalid hex length " + bl + " [" + str + "]");
        bl /= 2;
        byte b[] = new byte[bl];
        for (int i = 0, il = str.length(), bi = 0; i < il; i++) {
            char ch = str.charAt(i);
            if (!Character.isWhitespace(ch)) {
                int bx = 0;
                if (ch >= '0' && ch <= '9') {
                    bx = (ch - '0') * 16;
                } else if (ch >= 'a' && ch <= 'f') {
                    bx = (ch - 'a' + 10) * 16;
                } else if (ch >= 'A' && ch <= 'F') {
                    bx = (ch - 'A' + 10) * 16;
                }
                ch = str.charAt(++i);
                if (ch >= '0' && ch <= '9') {
                    bx += (ch - '0');
                } else if (ch >= 'a' && ch <= 'f') {
                    bx += (ch - 'a' + 10);
                } else if (ch >= 'A' && ch <= 'F') {
                    bx += (ch - 'A' + 10);
                }
                b[bi++] = (byte) bx;
            }
        }
        return b;
    }

    public static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte bb[] = new byte[1024];
        int len;
        while ((len = in.read(bb)) >= 0) {
            out.write(bb, 0, len);
        }
        return out.toByteArray();
    }

    static ThreadLocal<Integer> dumpTab = new ThreadLocal<Integer>();

    public static String dump(byte bb[], int offset, int length) {
        boolean ascii = true;
        for (int i = offset, il = offset + length; i < il && ascii; i++) {
            byte b = bb[i];
            ascii = (b >= 10 && b < 128);
        }
        if (ascii) {
            try {
                return length + " [" + URLEncoder.encode(new String(bb, offset, length), "UTF8").replaceAll("\\+", " ") + "]";
            } catch (UnsupportedEncodingException e) {
                return "ERROR<<:" + e.getMessage() + ">>";
            }
        } else {
            return length + " HEX [" + toHex(bb, offset, length) + "]";
        }
    }

    public static String dump(Object obj) {
        if (dumpTab.get() != null) throw new RuntimeException("RECURSIVE CALL");
        dumpTab.set(0);
        try {
            DumpOutputStream dos = new DumpOutputStream();
            PrintWriter out = new PrintWriter(dos);

            dump(out, null, obj, new IdentityHashMap<Object, String>(), 0);

            out.close();
            return new String(dos.toByteArray());
        } finally {
            dumpTab.remove();
        }
    }

    public static String dumpName(Class<?> cz) {
        if (cz.isArray()) { return dumpName(cz.getComponentType()) + "[]"; }
        return cz.getName();
    }

    public static void dump(PrintWriter out, Object ref, Object o, IdentityHashMap<Object, String> rec, int depth) {
        String recID = rec.get(o);
        if (o != null && recID == null) {
            rec.put(o, dumpName(o.getClass()) + "@" + Integer.toHexString(System.identityHashCode(o)).toUpperCase());
        }
        if (o == null) {
            if (ref instanceof Method) {
                out.println(dumpName(((Method) ref).getReturnType()) + " NULL");
            } else {
                out.println("NULL");
            }
        } else if (o instanceof Number) {
            if (ref instanceof Method) {
                Format f = (Format) ((Method) ref).getAnnotation(Format.class);
                if (f != null) {
                    out.println(dumpName(o.getClass()) + " [" + new DecimalFormat(f.value()).format(o) + "]");
                } else {
                    out.println(dumpName(o.getClass()) + " [" + new DecimalFormat("#,##0.#####################").format(o) + "]");
                }
            } else {
                out.println(dumpName(o.getClass()) + " [" + new DecimalFormat("#,##0.#####################").format(o) + "]");
            }
        } else if (o instanceof String) {
            out.println(dumpName(o.getClass()) + " [" + o + "]");
        } else if (o instanceof Date || o instanceof Calendar) {
            if (ref instanceof Method) {
                Format f = (Format) ((Method) ref).getAnnotation(Format.class);
                if (f != null) {
                    out.println(dumpName(o.getClass()) + " [" + new SimpleDateFormat(f.value()).format(o) + "]");
                } else {
                    out.println(dumpName(o.getClass()) + " [" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(o) + "]");
                }
            } else {
                out.println(dumpName(o.getClass()) + " [" + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").format(o) + "]");
            }
        } else if (recID != null) {
            out.println(dumpName(o.getClass()) + " RECURSIVE " + recID);
        } else if (depth > MAX_DUMP_DEPTH) {
            out.println("TOO DEEP " + dumpName(o.getClass()) + "@" + Integer.toHexString(System.identityHashCode(o)).toUpperCase());
        } else if (o instanceof byte[]) {
            byte bb[] = (byte[]) o;
            boolean ascii = true;
            for (int i = 0, il = bb.length; i < il && ascii; i++) {
                byte b = bb[i];
                ascii = (b >= 10 && b < 128);
            }
            if (ascii) {
                try {
                    out.println(dumpName(o.getClass()) + " " + bb.length + " [" + URLEncoder.encode(new String(bb), "UTF8").replaceAll("\\+", " ") + "]");
                } catch (UnsupportedEncodingException e) {}
            } else {
                out.println(dumpName(o.getClass()) + " " + bb.length + " HEX [" + toHex(bb) + "]");
            }
        } else if (o instanceof Key) {
            out.println(dumpName(o.getClass()) + " HEX [" + toHex(((Key) o).getEncoded()) + "]");
        } else if (o instanceof Source) {
            try {
                out.flush();
                out.println();
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform((Source) o, new StreamResult(out));
                out.flush();
            } catch (Exception e) {
                out.print(" {exception:" + e.getMessage() + "} ");
            }
        } else if (o instanceof Node) {
            try {
                out.flush();
                out.println();
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                transformer.transform(new DOMSource((Node) o), new StreamResult(out));
                out.flush();
            } catch (Exception e) {
                out.print(" {exception:" + e.getMessage() + "} ");
            }
        } else if (o.getClass().isArray()) {
            int len = Array.getLength(o);
            if (len == 0) {
                out.println(dumpName(o.getClass()) + " [size:" + len + "]");
            } else {
                out.flush();
                dumpTab.set(dumpTab.get() + 1);
                out.println(dumpName(o.getClass()) + " [size:" + len);
                for (int i = 0, idx = 1; i < len; i++, idx++) {
                    out.print(idx + " = ");
                    dump(out, null, Array.get(o, i), rec, depth + 1);
                }
                out.flush();
                dumpTab.set(dumpTab.get() - 1);
                out.println("]");
            }
            // } else if (o.getClass().getName().startsWith("org.apache.")) {
            // out.println(dumpName(o.getClass())+" ["+o+"]");
        } else if (o instanceof Enumeration<?>) {
            Enumeration<?> en = (Enumeration<?>) o;
            out.flush();
            dumpTab.set(dumpTab.get() + 1);
            out.println(dumpName(o.getClass()) + " [");
            int idx = 1;
            while (en.hasMoreElements()) {
                Object co = en.nextElement();
                out.print(idx + " = ");
                dump(out, null, co, rec, depth + 1);
                idx++;
            }
            out.flush();
            dumpTab.set(dumpTab.get() - 1);
            out.println("]");
        } else if (o instanceof Collection<?>) {
            Collection<?> col = (Collection<?>) o;
            out.flush();
            dumpTab.set(dumpTab.get() + 1);
            out.println(dumpName(o.getClass()) + " [size:" + col.size());
            int idx = 1;
            for (Object co : col) {
                out.print(idx + " = ");
                dump(out, null, co, rec, depth + 1);
                idx++;
            }
            out.flush();
            dumpTab.set(dumpTab.get() - 1);
            out.println("]");
        } else if (o instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) o;
            out.flush();
            dumpTab.set(dumpTab.get() + 1);
            out.println(dumpName(o.getClass()) + " [size:" + map.size());
            for (Map.Entry<?, ?> me : map.entrySet()) {
                out.print("[" + me.getKey().toString() + "] = ");
                dump(out, null, me.getValue(), rec, depth + 1);
            }
            out.flush();
            dumpTab.set(dumpTab.get() - 1);
            out.println("]:MAP(" + o.getClass().getName() + ")");
        } else if (o.getClass().getName().startsWith("java")) {
            out.println(dumpName(o.getClass()) + " [" + o + "]");
        } else {
            try {
                Method m = o.getClass().getMethod("dump", PrintWriter.class);
                m.invoke(o, out);
                return;
            } catch (NoSuchMethodException e) {
                // IGNORE
            } catch (Exception e) {
                out.print(" {exception:" + e.getMessage() + "} ");
            }
            out.flush();
            dumpTab.set(dumpTab.get() + 1);
            out.println(dumpName(o.getClass()) + "@" + Integer.toHexString(System.identityHashCode(o)).toUpperCase() + " {");
            boolean notIgnoreNull = o.getClass().getAnnotation(IgnoreNull.class) == null;
            for (Method m : o.getClass().getMethods()) {
                String mn = m.getName();
                if (m.getDeclaringClass().getName().startsWith("java")) {
                    // SKIP
                } else if (m.getAnnotation(Ignore.class) != null) {
                    // Ignore
                } else if (m.getAnnotation(SystemID.class) != null) {
                    try {
                        Object result = m.invoke(o);
                        if (notIgnoreNull || result != null) {
                            if (m.getName().startsWith("get")) {
                                out.print(Character.toLowerCase(mn.charAt(3)) + mn.substring(4));
                            } else {
                                out.print(Character.toLowerCase(mn.charAt(2)) + mn.substring(3));
                            }
                            out.print(" = ");
                            if (result != null) {
                                out.println(dumpName(result.getClass()) + "@" + Integer.toHexString(System.identityHashCode(result)).toUpperCase());
                            } else {
                                out.println("NULL");
                            }
                        }
                    } catch (Exception e) {
                        // IGNORE
                    }
                } else if (!m.getReturnType().equals(Void.class) && m.getParameterTypes().length == 0 && ((mn.startsWith("get") && mn.length() > 3) || (mn.startsWith("is") && mn.length() > 2))) {
                    try {
                        Object result = m.invoke(o);
                        if (notIgnoreNull || result != null) {
                            if (m.getName().startsWith("get")) {
                                out.print(Character.toLowerCase(mn.charAt(3)) + mn.substring(4));
                            } else {
                                out.print(Character.toLowerCase(mn.charAt(2)) + mn.substring(3));
                            }
                            out.print(" = ");
                            dump(out, m, result, rec, depth + 1);
                        }
                    } catch (Exception e) {
                        // IGNORE
                    }
                }
            }
            out.flush();
            dumpTab.set(dumpTab.get() - 1);
            out.println("}");
            // out.println(o.getClass().getName()+" ["+o+"]");
        }
    }

    public static class DumpOutputStream extends OutputStream {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean nl = false;
        int p = 0;
        byte tabz[] = "                                                                                                            ".getBytes();

        @Override
        public void write(byte[] bb, int off, int len) throws IOException {
            int tab = dumpTab.get();
            for (int i = 0, p = off; i < len; i++, p++) {
                byte b = bb[p];
                if (nl) {
                    if (b == '\n' || b == '\r') {
                        out.write(b);
                    } else {
                        out.write(tabz, 0, Math.min(tab * 3, tabz.length));
                        out.write(b);
                        nl = false;
                    }
                } else if (b == '\n') {
                    out.write(b);
                    nl = true;
                } else {
                    out.write(b);
                }
            }
        }

        @Override
        public void write(int b) throws IOException {
            int tab = dumpTab.get();
            if (nl) {
                if (b == '\n' || b == '\r') {
                    out.write(b);
                } else {
                    out.write(tabz, 0, Math.min(tab * 3, tabz.length));
                    out.write(b);
                    nl = false;
                }
            } else if (b == '\n') {
                out.write(b);
                nl = true;
            } else {
                out.write(b);
            }
        }

        @Override
        public void close() throws IOException {
            out.close();
            super.close();
        }

        public byte[] toByteArray() {
            return out.toByteArray();
        }
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Ignore {}

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Format {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SystemID {

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnoreNull {}

    public interface Streamer {
        void stream(InputStream in);
    }

    public static void open(File file, Streamer streamer) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            streamer.stream(in);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {}
            }
        }
    }

    public static void open(String name, Streamer streamer) {
        InputStream in = null;
        try {
            in = new FileInputStream(name);
            streamer.stream(in);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {}
            }
        }
    }
}

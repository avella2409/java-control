package control;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Serializables {

    private Serializables() {
    }

    public static byte[] serialize(Object obj) {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream();
             ObjectOutputStream stream = new ObjectOutputStream(buf)) {
            stream.writeObject(obj);
            return buf.toByteArray();
        } catch (IOException x) {
            throw new IllegalStateException("Error serializing object", x);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] objectData) {
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(objectData))) {
            return (T) stream.readObject();
        } catch (IOException | ClassNotFoundException x) {
            throw new IllegalStateException("Error deserializing object", x);
        }
    }

    public static void callReadObject(Object o) throws Throwable {
        final byte[] objectData = Serializables.serialize(o);
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(objectData))) {
            final Method method = o.getClass().getDeclaredMethod("readObject", ObjectInputStream.class);
            method.setAccessible(true);
            try {
                method.invoke(o, stream);
            } catch (InvocationTargetException x) {
                throw (x.getCause() != null) ? x.getCause() : x;
            }
        }
    }
}

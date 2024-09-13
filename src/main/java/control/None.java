package control;

import java.io.Serializable;
import java.util.NoSuchElementException;

public record None<T>() implements Option<T>, Serializable {

    private static final long serialVersionUID = 1L;

    static final None<?> INSTANCE = new None<>();

    @Override
    public T get() {
        throw new NoSuchElementException("No value present");
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    private Object readResolve() {
        return INSTANCE;
    }
}
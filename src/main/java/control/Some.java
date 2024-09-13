package control;

import java.io.Serializable;

public record Some<T>(T value) implements Option<T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}

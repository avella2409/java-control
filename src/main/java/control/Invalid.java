package control;

import java.io.Serializable;
import java.util.NoSuchElementException;

public record Invalid<E, T>(E error) implements Validation<E, T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean isInvalid() {
        return true;
    }

    @Override
    public T get() throws RuntimeException {
        throw new NoSuchElementException("get of 'invalid' Validation");
    }

    @Override
    public E getError() {
        return error;
    }

}
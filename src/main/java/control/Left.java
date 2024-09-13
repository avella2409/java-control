package control;

import java.io.Serializable;
import java.util.NoSuchElementException;

public record Left<L, R>(L value) implements Either<L, R>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public R get() {
        throw new NoSuchElementException("get() on Left");
    }

    @Override
    public L getLeft() {
        return value;
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }
}
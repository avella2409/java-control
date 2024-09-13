package control;

import java.io.Serializable;
import java.util.Objects;

public record Failure<T>(Throwable cause) implements Try<T>, Serializable {

    private static final long serialVersionUID = 1L;

    public Failure {
        Objects.requireNonNull(cause, "cause is null");
        if (isFatal(cause)) {
            sneakyThrow(cause);
        }
    }

    private static boolean isFatal(Throwable throwable) {
        return throwable instanceof InterruptedException
                || throwable instanceof LinkageError
                || throwable instanceof ThreadDeath
                || throwable instanceof VirtualMachineError;
    }

    // DEV-NOTE: we do not plan to expose this as public API
    @SuppressWarnings("unchecked")
    private static <T extends Throwable, R> R sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    @Override
    public T get() {
        return sneakyThrow(cause);
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }
}
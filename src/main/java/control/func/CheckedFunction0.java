package control.func;

@FunctionalInterface
public interface CheckedFunction0<R> {
    R apply() throws Throwable;
}

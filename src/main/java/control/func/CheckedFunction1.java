package control.func;

@FunctionalInterface
public interface CheckedFunction1<T1, R> {
    R apply(T1 t1) throws Throwable;
}

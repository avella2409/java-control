package control.func;

import java.util.function.Function;

@FunctionalInterface
public interface Function1<T1, R> extends Function<T1, R> {
    R apply(T1 t1);
}

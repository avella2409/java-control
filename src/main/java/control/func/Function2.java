package control.func;

import java.util.function.BiFunction;

@FunctionalInterface
public interface Function2<T1, T2, R> extends BiFunction<T1, T2, R> {
    R apply(T1 t1, T2 t2);
}

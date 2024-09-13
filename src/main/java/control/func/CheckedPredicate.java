package control.func;

import java.util.function.Predicate;

@FunctionalInterface
public interface CheckedPredicate<T> {
    boolean test(T t) throws Throwable;
    default CheckedPredicate<T> negate() {
        return t -> !test(t);
    }
}

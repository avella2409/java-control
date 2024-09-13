package control.func;

import java.util.function.Supplier;

@FunctionalInterface
public interface Function0<R> extends Supplier<R> {

    R apply();

    @Override
    default R get() {
        return apply();
    }
}

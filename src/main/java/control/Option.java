package control;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface Option<T> extends Control<T>, Serializable permits Some, None {

    static <T> Option<T> direct(Function<OptionExtractor, T> body) {
        return Boundary.apply(label -> some(body.apply(new LabelOptionExtractor(label))));
    }

    default T value(OptionExtractor $) {
        return $.value(this);
    }

    static <T> Option<T> ofNullable(T value) {
        return value == null ? none() : some(value);
    }

    static <T> Option<List<T>> sequence(Iterable<? extends Option<? extends T>> values) {
        Objects.requireNonNull(values, "values is null");

        List<T> list = new ArrayList<>();
        for (Option<? extends T> value : values) {
            if (value.isEmpty()) return Option.none();
            list.add(value.get());
        }
        return Option.some(list);
    }

    static <T> Option<T> some(T value) {
        Objects.requireNonNull(value, "value is null");
        return new Some<>(value);
    }

    @SuppressWarnings("unchecked")
    static <T> Option<T> none() {
        return (None<T>) None.INSTANCE;
    }

    static <T> Option<T> when(boolean condition, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return condition ? some(supplier.get()) : none();
    }

    static <T> Option<T> when(boolean condition, T value) {
        return condition ? some(value) : none();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Option<T> ofOptional(Optional<? extends T> optional) {
        Objects.requireNonNull(optional, "optional is null");
        return optional.<Option<T>>map(Option::ofNullable).orElseGet(Option::none);
    }

    @Override
    boolean isEmpty();

    default Option<T> onEmpty(Runnable action) {
        Objects.requireNonNull(action, "action is null");
        if (isEmpty()) {
            action.run();
        }
        return this;
    }

    @Override
    T get();

    @Override
    default T getOrElse(T other) {
        return isEmpty() ? other : get();
    }

    @SuppressWarnings("unchecked")
    default Option<T> orElse(Option<? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return isEmpty() ? (Option<T>) other : this;
    }

    @SuppressWarnings("unchecked")
    default Option<T> orElse(Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isEmpty() ? (Option<T>) supplier.get() : this;
    }

    default T orNull() {
        return isEmpty() ? null : get();
    }

    @Override
    default T getOrElse(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isEmpty() ? supplier.get() : get();
    }

    @Override
    default <X extends Throwable> T getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier is null");
        if (isEmpty()) {
            throw exceptionSupplier.get();
        } else {
            return get();
        }
    }

    default Option<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return isEmpty() || predicate.test(get()) ? this : none();
    }

    default Option<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    @SuppressWarnings("unchecked")
    default <U> Option<U> flatMap(Function<? super T, ? extends Option<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? none() : (Option<U>) mapper.apply(get());
    }

    @Override
    default <U> Option<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? none() : some(mapper.apply(get()));
    }

    default <U> U fold(Supplier<? extends U> ifNone, Function<? super T, ? extends U> f) {
        return this.<U>map(f).getOrElse(ifNone);
    }

    default Option<T> peek(Runnable noneAction, Consumer<? super T> someAction) {
        Objects.requireNonNull(noneAction, "noneAction is null");
        Objects.requireNonNull(someAction, "someAction is null");

        if (isEmpty()) {
            noneAction.run();
        } else {
            someAction.accept(get());
        }

        return this;
    }

    @Override
    default Option<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (isPresent()) action.accept(get());
        return this;
    }

    default <U> U transform(Function<? super Option<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    default Iterator<T> iterator() {
        return isEmpty() ? Iterators.empty() : Iterators.of(get());
    }

}

final class LabelOptionExtractor implements OptionExtractor {

    private final Label label;

    public LabelOptionExtractor(Label label) {
        this.label = label;
    }

    @Override
    public <T> T value(Option<T> option) {
        if (option.isEmpty()) {
            // Break flow / Short Circuit, go to boundary definition
            Boundary.breakNow(option, label);
            return null;
        } else return option.get();
    }
}

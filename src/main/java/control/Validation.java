package control;

import control.func.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface Validation<E, T> extends Control<T>, Serializable permits Valid, Invalid {

    static <E, T> Validation<E, T> direct(Function<ValidationExtractor<E>, T> body) {
        return Boundary.apply(label -> valid(body.apply(new LabelValidationExtractor<>(label))));
    }

    default T value(ValidationExtractor<E> $) {
        return $.value(this);
    }

    static <E, T> Validation<E, T> ofNullable(T value, E error) {
        return value == null ? invalid(error) : valid(value);
    }

    static <E, T> Validation<E, T> valid(T value) {
        Objects.requireNonNull(value, "value is null");
        return new Valid<>(value);
    }

    static <E, T> Validation<E, T> invalid(E error) {
        Objects.requireNonNull(error, "error is null");
        return new Invalid<>(error);
    }

    static <E, T> Validation<E, T> fromEither(Either<E, T> either) {
        Objects.requireNonNull(either, "either is null");
        return either.isRight() ? valid(either.get()) : invalid(either.getLeft());
    }

    static <T> Validation<Throwable, T> fromTry(Try<? extends T> t) {
        Objects.requireNonNull(t, "t is null");
        return t.isSuccess() ? valid(t.get()) : invalid(t.getCause());
    }

    static <E, T> Validation<List<E>, List<T>> sequence(Iterable<? extends Validation<? extends List<? extends E>, ? extends T>> values) {
        Objects.requireNonNull(values, "values is null");
        List<E> invalids = new ArrayList<>();
        List<T> valids = new ArrayList<>();
        for (Validation<? extends List<? extends E>, ? extends T> value : values) {
            if (value.isInvalid()) invalids.addAll(value.getError());
            else valids.add(value.get());
        }
        return invalids.isEmpty() ? valid(valids) : invalid(invalids);
    }

    default <U> U transform(Function<? super Validation<E, T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    static <E, T1, T2> Builder<E, T1, T2> combine(Validation<E, T1> validation1, Validation<E, T2> validation2) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        return new Builder<>(validation1, validation2);
    }

    static <E, T1, T2, T3> Builder3<E, T1, T2, T3> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        return new Builder3<>(validation1, validation2, validation3);
    }

    static <E, T1, T2, T3, T4> Builder4<E, T1, T2, T3, T4> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        return new Builder4<>(validation1, validation2, validation3, validation4);
    }

    static <E, T1, T2, T3, T4, T5> Builder5<E, T1, T2, T3, T4, T5> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        return new Builder5<>(validation1, validation2, validation3, validation4, validation5);
    }

    static <E, T1, T2, T3, T4, T5, T6> Builder6<E, T1, T2, T3, T4, T5, T6> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5, Validation<E, T6> validation6) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        Objects.requireNonNull(validation6, "validation6 is null");
        return new Builder6<>(validation1, validation2, validation3, validation4, validation5, validation6);
    }

    static <E, T1, T2, T3, T4, T5, T6, T7> Builder7<E, T1, T2, T3, T4, T5, T6, T7> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5, Validation<E, T6> validation6, Validation<E, T7> validation7) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        Objects.requireNonNull(validation6, "validation6 is null");
        Objects.requireNonNull(validation7, "validation7 is null");
        return new Builder7<>(validation1, validation2, validation3, validation4, validation5, validation6, validation7);
    }

    static <E, T1, T2, T3, T4, T5, T6, T7, T8> Builder8<E, T1, T2, T3, T4, T5, T6, T7, T8> combine(Validation<E, T1> validation1, Validation<E, T2> validation2, Validation<E, T3> validation3, Validation<E, T4> validation4, Validation<E, T5> validation5, Validation<E, T6> validation6, Validation<E, T7> validation7, Validation<E, T8> validation8) {
        Objects.requireNonNull(validation1, "validation1 is null");
        Objects.requireNonNull(validation2, "validation2 is null");
        Objects.requireNonNull(validation3, "validation3 is null");
        Objects.requireNonNull(validation4, "validation4 is null");
        Objects.requireNonNull(validation5, "validation5 is null");
        Objects.requireNonNull(validation6, "validation6 is null");
        Objects.requireNonNull(validation7, "validation7 is null");
        Objects.requireNonNull(validation8, "validation8 is null");
        return new Builder8<>(validation1, validation2, validation3, validation4, validation5, validation6, validation7, validation8);
    }

    boolean isValid();

    boolean isInvalid();

    @SuppressWarnings("unchecked")
    default Validation<E, T> orElse(Validation<? extends E, ? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return isValid() ? this : (Validation<E, T>) other;
    }

    @SuppressWarnings("unchecked")
    default Validation<E, T> orElse(Supplier<Validation<? extends E, ? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isValid() ? this : (Validation<E, T>) supplier.get();
    }

    @Override
    default boolean isEmpty() {
        return isInvalid();
    }

    @Override
    T get();

    default T getOrElseGet(Function<? super E, ? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        if (isValid()) {
            return get();
        } else {
            return other.apply(getError());
        }
    }

    E getError();

    default Either<E, T> toEither() {
        return isValid() ? Either.right(get()) : Either.left(getError());
    }

    @Override
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (isValid()) {
            action.accept(get());
        }
    }

    default <U> U fold(Function<? super E, ? extends U> ifInvalid, Function<? super T, ? extends U> ifValid) {
        Objects.requireNonNull(ifInvalid, "ifInvalid is null");
        Objects.requireNonNull(ifValid, "ifValid is null");
        return isValid() ? ifValid.apply(get()) : ifInvalid.apply(getError());
    }

    default <X extends Throwable> T getOrElseThrow(Function<? super E, X> exceptionFunction) throws X {
        Objects.requireNonNull(exceptionFunction, "exceptionFunction is null");
        if (isValid()) {
            return get();
        } else {
            throw exceptionFunction.apply(getError());
        }
    }

    default Validation<T, E> swap() {
        if (isInvalid()) {
            final E error = this.getError();
            return Validation.valid(error);
        } else {
            final T value = this.get();
            return Validation.invalid(value);
        }
    }

    @Override
    default <U> Validation<E, U> map(Function<? super T, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (isInvalid()) {
            return Validation.invalid(this.getError());
        } else {
            final T value = this.get();
            return Validation.valid(f.apply(value));
        }
    }

    default <E2, T2> Validation<E2, T2> bimap(Function<? super E, ? extends E2> errorMapper, Function<? super T, ? extends T2> valueMapper) {
        Objects.requireNonNull(errorMapper, "errorMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        if (isInvalid()) {
            final E error = this.getError();
            return Validation.invalid(errorMapper.apply(error));
        } else {
            final T value = this.get();
            return Validation.valid(valueMapper.apply(value));
        }
    }

    default <U> Validation<U, T> mapError(Function<? super E, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (isInvalid()) {
            final E error = this.getError();
            return Validation.invalid(f.apply(error));
        } else {
            return Validation.valid(this.get());
        }
    }

    default <U> Validation<List<E>, U> ap(Validation<List<E>, ? extends Function<? super T, ? extends U>> validation) {
        Objects.requireNonNull(validation, "validation is null");
        if (isValid()) {
            if (validation.isValid()) {
                final Function<? super T, ? extends U> f = validation.get();
                final U u = f.apply(this.get());
                return valid(u);
            } else return invalid(validation.getError());
        } else {
            if (validation.isValid()) {
                final E error = this.getError();
                return invalid(List.of(error));
            } else {
                final List<E> errors = new ArrayList<>(validation.getError());
                final E error = this.getError();
                errors.add(error);
                return invalid(errors);
            }
        }
    }

    default <U> Builder<E, T, U> combine(Validation<E, U> validation) {
        return new Builder<>(this, validation);
    }

    default Option<Validation<E, T>> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return isInvalid() || predicate.test(get()) ? Option.some(this) : Option.none();
    }

    @SuppressWarnings("unchecked")
    default <U> Validation<E, U> flatMap(Function<? super T, ? extends Validation<E, ? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isInvalid() ? (Validation<E, U>) this : (Validation<E, U>) mapper.apply(get());
    }

    @Override
    default Iterator<T> iterator() {
        return isValid() ? Iterators.of(get()) : Iterators.empty();
    }

    default Validation<E, T> peek(Consumer<? super E> invalidAction, Consumer<? super T> validAction) {
        Objects.requireNonNull(invalidAction, "invalidAction is null");
        Objects.requireNonNull(validAction, "validAction is null");

        if (isInvalid()) {
            invalidAction.accept(getError());
        } else {
            validAction.accept(get());
        }

        return this;
    }

    @Override
    default Validation<E, T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");

        if (isValid()) {
            action.accept(get());
        }
        return this;
    }

    default Validation<E, T> peekError(Consumer<? super E> action) {
        Objects.requireNonNull(action, "action is null");

        if (isInvalid()) {
            action.accept(getError());
        }
        return this;
    }

    final class Builder<E, T1, T2> {

        private Validation<E, T1> v1;
        private Validation<E, T2> v2;

        private Builder(Validation<E, T1> v1, Validation<E, T2> v2) {
            this.v1 = v1;
            this.v2 = v2;
        }

        public <R> Validation<List<E>, R> ap(Function2<T1, T2, R> f) {
            List<E> errors = new ArrayList<>();
            if (v1.isInvalid()) errors.add(v1.getError());
            if (v2.isInvalid()) errors.add(v2.getError());

            return errors.isEmpty() ?
                    Validation.valid(f.apply(v1.get(), v2.get()))
                    : Validation.invalid(errors);
        }

        public <T3> Builder3<E, T1, T2, T3> combine(Validation<E, T3> v3) {
            return new Builder3<>(v1, v2, v3);
        }

    }

    final class Builder3<E, T1, T2, T3> {

        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;

        private Builder3(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
        }

        public <R> Validation<List<E>, R> ap(Function3<T1, T2, T3, R> f) {
            List<E> errors = new ArrayList<>();
            if (v1.isInvalid()) errors.add(v1.getError());
            if (v2.isInvalid()) errors.add(v2.getError());
            if (v3.isInvalid()) errors.add(v3.getError());

            return errors.isEmpty() ?
                    Validation.valid(f.apply(v1.get(), v2.get(), v3.get()))
                    : Validation.invalid(errors);
        }

        public <T4> Builder4<E, T1, T2, T3, T4> combine(Validation<E, T4> v4) {
            return new Builder4<>(v1, v2, v3, v4);
        }

    }

    final class Builder4<E, T1, T2, T3, T4> {

        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;

        private Builder4(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        public <R> Validation<List<E>, R> ap(Function4<T1, T2, T3, T4, R> f) {
            List<E> errors = new ArrayList<>();
            if (v1.isInvalid()) errors.add(v1.getError());
            if (v2.isInvalid()) errors.add(v2.getError());
            if (v3.isInvalid()) errors.add(v3.getError());
            if (v4.isInvalid()) errors.add(v4.getError());

            return errors.isEmpty() ?
                    Validation.valid(f.apply(v1.get(), v2.get(), v3.get(), v4.get()))
                    : Validation.invalid(errors);
        }

        public <T5> Builder5<E, T1, T2, T3, T4, T5> combine(Validation<E, T5> v5) {
            return new Builder5<>(v1, v2, v3, v4, v5);
        }

    }

    final class Builder5<E, T1, T2, T3, T4, T5> {

        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;

        private Builder5(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
        }

        public <R> Validation<List<E>, R> ap(Function5<T1, T2, T3, T4, T5, R> f) {
            List<E> errors = new ArrayList<>();
            if (v1.isInvalid()) errors.add(v1.getError());
            if (v2.isInvalid()) errors.add(v2.getError());
            if (v3.isInvalid()) errors.add(v3.getError());
            if (v4.isInvalid()) errors.add(v4.getError());
            if (v5.isInvalid()) errors.add(v5.getError());

            return errors.isEmpty() ?
                    Validation.valid(f.apply(v1.get(), v2.get(), v3.get(), v4.get(), v5.get()))
                    : Validation.invalid(errors);
        }

        public <T6> Builder6<E, T1, T2, T3, T4, T5, T6> combine(Validation<E, T6> v6) {
            return new Builder6<>(v1, v2, v3, v4, v5, v6);
        }

    }

    final class Builder6<E, T1, T2, T3, T4, T5, T6> {

        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;
        private Validation<E, T6> v6;

        private Builder6(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
        }

        public <R> Validation<List<E>, R> ap(Function6<T1, T2, T3, T4, T5, T6, R> f) {
            List<E> errors = new ArrayList<>();
            if (v1.isInvalid()) errors.add(v1.getError());
            if (v2.isInvalid()) errors.add(v2.getError());
            if (v3.isInvalid()) errors.add(v3.getError());
            if (v4.isInvalid()) errors.add(v4.getError());
            if (v5.isInvalid()) errors.add(v5.getError());
            if (v6.isInvalid()) errors.add(v6.getError());

            return errors.isEmpty() ?
                    Validation.valid(f.apply(v1.get(), v2.get(), v3.get(), v4.get(), v5.get(), v6.get()))
                    : Validation.invalid(errors);
        }

        public <T7> Builder7<E, T1, T2, T3, T4, T5, T6, T7> combine(Validation<E, T7> v7) {
            return new Builder7<>(v1, v2, v3, v4, v5, v6, v7);
        }

    }

    final class Builder7<E, T1, T2, T3, T4, T5, T6, T7> {

        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;
        private Validation<E, T6> v6;
        private Validation<E, T7> v7;

        private Builder7(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6, Validation<E, T7> v7) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
        }

        public <R> Validation<List<E>, R> ap(Function7<T1, T2, T3, T4, T5, T6, T7, R> f) {
            List<E> errors = new ArrayList<>();
            if (v1.isInvalid()) errors.add(v1.getError());
            if (v2.isInvalid()) errors.add(v2.getError());
            if (v3.isInvalid()) errors.add(v3.getError());
            if (v4.isInvalid()) errors.add(v4.getError());
            if (v5.isInvalid()) errors.add(v5.getError());
            if (v6.isInvalid()) errors.add(v6.getError());
            if (v7.isInvalid()) errors.add(v7.getError());

            return errors.isEmpty() ?
                    Validation.valid(f.apply(v1.get(), v2.get(), v3.get(), v4.get(), v5.get(), v6.get(), v7.get()))
                    : Validation.invalid(errors);
        }

        public <T8> Builder8<E, T1, T2, T3, T4, T5, T6, T7, T8> combine(Validation<E, T8> v8) {
            return new Builder8<>(v1, v2, v3, v4, v5, v6, v7, v8);
        }

    }

    final class Builder8<E, T1, T2, T3, T4, T5, T6, T7, T8> {

        private Validation<E, T1> v1;
        private Validation<E, T2> v2;
        private Validation<E, T3> v3;
        private Validation<E, T4> v4;
        private Validation<E, T5> v5;
        private Validation<E, T6> v6;
        private Validation<E, T7> v7;
        private Validation<E, T8> v8;

        private Builder8(Validation<E, T1> v1, Validation<E, T2> v2, Validation<E, T3> v3, Validation<E, T4> v4, Validation<E, T5> v5, Validation<E, T6> v6, Validation<E, T7> v7, Validation<E, T8> v8) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.v5 = v5;
            this.v6 = v6;
            this.v7 = v7;
            this.v8 = v8;
        }

        public <R> Validation<List<E>, R> ap(Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> f) {
            List<E> errors = new ArrayList<>();
            if (v1.isInvalid()) errors.add(v1.getError());
            if (v2.isInvalid()) errors.add(v2.getError());
            if (v3.isInvalid()) errors.add(v3.getError());
            if (v4.isInvalid()) errors.add(v4.getError());
            if (v5.isInvalid()) errors.add(v5.getError());
            if (v6.isInvalid()) errors.add(v6.getError());
            if (v7.isInvalid()) errors.add(v7.getError());
            if (v8.isInvalid()) errors.add(v8.getError());

            return errors.isEmpty() ?
                    Validation.valid(f.apply(v1.get(), v2.get(), v3.get(), v4.get(), v5.get(), v6.get(), v7.get(), v8.get()))
                    : Validation.invalid(errors);
        }
    }
}

final class LabelValidationExtractor<E> implements ValidationExtractor<E> {

    private final Label label;

    LabelValidationExtractor(Label label) {
        this.label = label;
    }

    @Override
    public <T> T value(Validation<E, T> validation) {
        if (validation.isInvalid()) {
            // Break flow / Short Circuit, go to boundary definition
            Boundary.breakNow(validation, label);
            return null;
        } else return validation.get();
    }
}

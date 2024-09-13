package control;

import control.func.CheckedFunction0;

import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface Control<T> extends Iterable<T> {

    T get();

    boolean isEmpty();

    default boolean isPresent() {
        return !isEmpty();
    }

    @Override
    default void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        for (T t : this) action.accept(t);
    }

    default T getOrElse(T other) {
        return isEmpty() ? other : get();
    }

    default T getOrElse(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isEmpty() ? supplier.get() : get();
    }

    default <X extends Throwable> T getOrElseThrow(Supplier<X> supplier) throws X {
        Objects.requireNonNull(supplier, "supplier is null");
        if (isEmpty()) throw supplier.get();
        else return get();
    }

    default T getOrElseTry(CheckedFunction0<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isEmpty() ? Try.of(supplier).get() : get();
    }

    default T getOrNull() {
        return isEmpty() ? null : get();
    }

    <U> Control<U> map(Function<? super T, ? extends U> mapper);

    Control<T> peek(Consumer<? super T> action);

    default Optional<T> toJavaOptional() {
        return isEmpty() ? Optional.empty() : Optional.ofNullable(get());
    }

    default <L> Either<L, T> toEither(L left) {
        if (this instanceof Either) {
            return ((Either<?, T>) this).mapLeft(ignored -> left);
        } else {
            return isEmpty() ? Either.left(left) : Either.right(get());
        }
    }

    default <L> Either<L, T> toEither(Supplier<? extends L> leftSupplier) {
        Objects.requireNonNull(leftSupplier, "leftSupplier is null");
        if (this instanceof Either) {
            return ((Either<?, T>) this).mapLeft(ignored -> leftSupplier.get());
        } else {
            return isEmpty() ? Either.left(leftSupplier.get()) : Either.right(get());
        }
    }

    default Option<T> toOption() {
        if (this instanceof Option) {
            return (Option<T>) this;
        } else {
            return isEmpty() ? Option.none() : Option.some(get());
        }
    }

    default <E> Validation<E, T> toValidation(E invalid) {
        if (this instanceof Validation) {
            return ((Validation<?, T>) this).mapError(ignored -> invalid);
        } else {
            return isEmpty() ? Validation.invalid(invalid) : Validation.valid(get());
        }
    }

    default <E> Validation<E, T> toValidation(Supplier<? extends E> invalidSupplier) {
        Objects.requireNonNull(invalidSupplier, "invalidSupplier is null");
        if (this instanceof Validation) {
            return ((Validation<?, T>) this).mapError(ignored -> invalidSupplier.get());
        } else {
            return isEmpty() ? Validation.invalid(invalidSupplier.get()) : Validation.valid(get());
        }
    }

    default Try<T> toTry() {
        if (this instanceof Try) {
            return (Try<T>) this;
        } else {
            return Try.of(this::get);
        }
    }

    default Try<T> toTry(Supplier<? extends Throwable> ifEmpty) {
        Objects.requireNonNull(ifEmpty, "ifEmpty is null");
        return isEmpty() ? Try.failure(ifEmpty.get()) : toTry();
    }

    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), isEmpty() ? 0 : 1,
                Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
    }
}

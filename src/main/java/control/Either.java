package control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface Either<L, R> extends Control<R>, Serializable permits Right, Left {

    static <L, R> Either<L, R> direct(Function<EitherExtractor<L>, R> body) {
        return Boundary.apply(label -> right(body.apply(new LabelEitherExtractor<>(label))));
    }

    default R value(EitherExtractor<L> $) {
        return $.value(this);
    }

    static <L, R> Either<L, R> ofNullable(R right, L left) {
        return right == null ? left(left) : right(right);
    }

    static <L, R> Either<L, R> right(R right) {
        Objects.requireNonNull(right, "right is null");
        return new Right<>(right);
    }

    static <L, R> Either<L, R> left(L left) {
        Objects.requireNonNull(left, "left is null");
        return new Left<>(left);
    }

    @Override
    default Iterator<R> iterator() {
        return isRight() ? Iterators.of(get()) : Iterators.empty();
    }

    L getLeft();

    boolean isLeft();

    boolean isRight();

    default <X, Y> Either<X, Y> bimap(Function<? super L, ? extends X> leftMapper, Function<? super R, ? extends Y> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        if (isRight()) {
            return new Right<>(rightMapper.apply(get()));
        } else {
            return new Left<>(leftMapper.apply(getLeft()));
        }
    }

    default <U> U fold(Function<? super L, ? extends U> leftMapper, Function<? super R, ? extends U> rightMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        Objects.requireNonNull(rightMapper, "rightMapper is null");
        if (isRight()) {
            return rightMapper.apply(get());
        } else {
            return leftMapper.apply(getLeft());
        }
    }

    @SuppressWarnings("unchecked")
    static <L, R> Either<List<L>, List<R>> sequence(Iterable<? extends Either<? extends L, ? extends R>> eithers) {
        Objects.requireNonNull(eithers, "eithers is null");
        List<L> lefts = new ArrayList<>();
        List<R> rights = new ArrayList<>();

        for (Either<? extends L, ? extends R> either : eithers) {
            if (either.isLeft()) lefts.add(either.getLeft());
            else rights.add(either.get());
        }

        return !lefts.isEmpty() ? Either.left(lefts) : Either.right(rights);
    }

    static <L, R> Either<L, List<R>> sequenceRight(Iterable<? extends Either<? extends L, ? extends R>> eithers) {
        Objects.requireNonNull(eithers, "eithers is null");
        List<R> rightValues = new ArrayList<>();

        for (Either<? extends L, ? extends R> either : eithers) {
            if (either.isRight()) rightValues.add(either.get());
            else return Either.left(either.getLeft());
        }
        return Either.right(rightValues);
    }

    default <U> U transform(Function<? super Either<L, R>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    default R getOrElseGet(Function<? super L, ? extends R> other) {
        Objects.requireNonNull(other, "other is null");
        if (isRight()) {
            return get();
        } else {
            return other.apply(getLeft());
        }
    }

    default void orElseRun(Consumer<? super L> action) {
        Objects.requireNonNull(action, "action is null");
        if (isLeft()) {
            action.accept(getLeft());
        }
    }

    default <X extends Throwable> R getOrElseThrow(Function<? super L, X> exceptionFunction) throws X {
        Objects.requireNonNull(exceptionFunction, "exceptionFunction is null");
        if (isRight()) {
            return get();
        } else {
            throw exceptionFunction.apply(getLeft());
        }
    }

    default Either<R, L> swap() {
        if (isRight()) {
            return new Left<>(get());
        } else {
            return new Right<>(getLeft());
        }
    }

    @SuppressWarnings("unchecked")
    default Either<L, R> recoverWith(Function<? super L, ? extends Either<? extends L, ? extends R>> recoveryFunction) {
        Objects.requireNonNull(recoveryFunction, "recoveryFunction is null");
        if (isLeft()) {
            return (Either<L, R>) recoveryFunction.apply(getLeft());
        } else {
            return this;
        }
    }

    default Either<L, R> recover(Function<? super L, ? extends R> recoveryFunction) {
        Objects.requireNonNull(recoveryFunction, "recoveryFunction is null");
        if (isLeft()) {
            return Either.right(recoveryFunction.apply(getLeft()));
        } else {
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    default <U> Either<L, U> flatMap(Function<? super R, ? extends Either<L, ? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isRight()) {
            return (Either<L, U>) mapper.apply(get());
        } else {
            return (Either<L, U>) this;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    default <U> Either<L, U> map(Function<? super R, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isRight()) {
            return Either.right(mapper.apply(get()));
        } else {
            return (Either<L, U>) this;
        }
    }

    @SuppressWarnings("unchecked")
    default <U> Either<U, R> mapLeft(Function<? super L, ? extends U> leftMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        if (isLeft()) {
            return Either.left(leftMapper.apply(getLeft()));
        } else {
            return (Either<U, R>) this;
        }
    }

    default Option<Either<L, R>> filter(Predicate<? super R> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return isLeft() || predicate.test(get()) ? Option.some(this) : Option.none();
    }

    default Option<Either<L, R>> filterNot(Predicate<? super R> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    default Either<L, R> filterOrElse(Predicate<? super R> predicate, Function<? super R, ? extends L> zero) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(zero, "zero is null");
        if (isLeft() || predicate.test(get())) {
            return this;
        } else {
            return Either.left(zero.apply(get()));
        }
    }

    @Override
    R get();

    @Override
    default boolean isEmpty() {
        return isLeft();
    }

    @SuppressWarnings("unchecked")
    default Either<L, R> orElse(Either<? extends L, ? extends R> other) {
        Objects.requireNonNull(other, "other is null");
        return isRight() ? this : (Either<L, R>) other;
    }

    @SuppressWarnings("unchecked")
    default Either<L, R> orElse(Supplier<? extends Either<? extends L, ? extends R>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isRight() ? this : (Either<L, R>) supplier.get();
    }

    default Either<L, R> peek(Consumer<? super L> leftAction, Consumer<? super R> rightAction) {
        Objects.requireNonNull(leftAction, "leftAction is null");
        Objects.requireNonNull(rightAction, "rightAction is null");

        if (isLeft()) {
            leftAction.accept(getLeft());
        } else { // this isRight() by definition
            rightAction.accept(get());
        }

        return this;
    }

    @Override
    default Either<L, R> peek(Consumer<? super R> action) {
        Objects.requireNonNull(action, "action is null");
        if (isRight()) {
            action.accept(get());
        }
        return this;
    }

    default Either<L, R> peekLeft(Consumer<? super L> action) {
        Objects.requireNonNull(action, "action is null");
        if (isLeft()) {
            action.accept(getLeft());
        }
        return this;
    }

    default Validation<L, R> toValidation() {
        return isRight() ? Validation.valid(get()) : Validation.invalid(getLeft());
    }

    default Try<R> toTry(Function<? super L, ? extends Throwable> leftMapper) {
        Objects.requireNonNull(leftMapper, "leftMapper is null");
        return mapLeft(leftMapper).fold(Try::failure, Try::success);
    }

}

final class LabelEitherExtractor<L> implements EitherExtractor<L> {

    private final Label label;

    LabelEitherExtractor(Label label) {
        this.label = label;
    }

    @Override
    public <R> R value(Either<L, R> either) {
        if (either.isLeft()) {
            // Break flow / Short Circuit, go to boundary definition
            Boundary.breakNow(either, label);
            return null;
        } else return either.get();
    }
}

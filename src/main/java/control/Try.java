package control;

import control.func.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public sealed interface Try<T> extends Control<T>, Serializable permits Success, Failure {

    static <T> Try<T> direct(Function<TryExtractor, T> body) {
        return Boundary.apply(label -> success(body.apply(new LabelTryExtractor(label))));
    }

    default T value(TryExtractor $) {
        return $.value(this);
    }

    static <T> Try<T> of(CheckedFunction0<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        try {
            return new Success<>(supplier.apply());
        } catch (Throwable t) {
            return new Failure<>(t);
        }
    }

    static <T> Try<T> ofSupplier(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return of(supplier::get);
    }

    static <T> Try<T> ofCallable(Callable<? extends T> callable) {
        Objects.requireNonNull(callable, "callable is null");
        return of(callable::call);
    }

    static Try<Void> run(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        try {
            runnable.run();
            return new Success<>(null); // null represents the absence of an value, i.e. Void
        } catch (Throwable t) {
            return new Failure<>(t);
        }
    }

    static Try<Void> runRunnable(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        return run(runnable::run);
    }

    static <T> Try<List<T>> sequence(Iterable<? extends Try<? extends T>> values) {
        Objects.requireNonNull(values, "values is null");
        List<T> list = new ArrayList<>();
        for (Try<? extends T> value : values) {
            if (value.isFailure()) return Try.failure(value.getCause());
            else list.add(value.get());
        }
        return Try.success(list);
    }

    static <T> Try<T> ofNullable(T value, Throwable exception) {
        return value == null ? failure(exception) : success(value);
    }

    static <T> Try<T> success(T value) {
        Objects.requireNonNull(value, "value is null");
        return new Success<>(value);
    }

    static <T> Try<T> failure(Throwable exception) {
        return new Failure<>(exception);
    }

    default Try<T> andThen(Consumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer is null");
        return andThenTry(consumer::accept);
    }

    default Try<T> andThenTry(CheckedConsumer<? super T> consumer) {
        Objects.requireNonNull(consumer, "consumer is null");
        if (isFailure()) {
            return this;
        } else {
            try {
                consumer.accept(get());
                return this;
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }
    }

    default Try<T> andThen(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        return andThenTry(runnable::run);
    }

    default Try<T> andThenTry(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        if (isFailure()) {
            return this;
        } else {
            try {
                runnable.run();
                return this;
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }
    }

    default Try<Throwable> failed() {
        if (isFailure()) {
            return new Success<>(getCause());
        } else {
            return new Failure<>(new NoSuchElementException("Success.failed()"));
        }
    }

    default Try<T> filter(Predicate<? super T> predicate, Supplier<? extends Throwable> throwableSupplier) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(throwableSupplier, "throwableSupplier is null");
        return filterTry(predicate::test, throwableSupplier);
    }

    default Try<T> filter(Predicate<? super T> predicate, Function<? super T, ? extends Throwable> errorProvider) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(errorProvider, "errorProvider is null");
        return filterTry(predicate::test, errorProvider::apply);
    }

    default Try<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filterTry(predicate::test);
    }

    default Try<T> filterNot(Predicate<? super T> predicate, Supplier<? extends Throwable> throwableSupplier) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate(), throwableSupplier);
    }

    default Try<T> filterNot(Predicate<? super T> predicate, Function<? super T, ? extends Throwable> errorProvider) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate(), errorProvider);
    }

    default Try<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    default Try<T> filterTry(CheckedPredicate<? super T> predicate, Supplier<? extends Throwable> throwableSupplier) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(throwableSupplier, "throwableSupplier is null");

        if (isFailure()) {
            return this;
        } else {
            try {
                if (predicate.test(get())) {
                    return this;
                } else {
                    return new Failure<>(throwableSupplier.get());
                }
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }
    }

    default Try<T> filterTry(CheckedPredicate<? super T> predicate, CheckedFunction1<? super T, ? extends Throwable> errorProvider) {
        Objects.requireNonNull(predicate, "predicate is null");
        Objects.requireNonNull(errorProvider, "errorProvider is null");
        return flatMapTry(t -> predicate.test(t) ? this : failure(errorProvider.apply(t)));
    }

    default Try<T> filterTry(CheckedPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filterTry(predicate, () -> new NoSuchElementException("Predicate does not hold for " + get()));
    }

    default <U> Try<U> flatMap(Function<? super T, ? extends Try<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return flatMapTry((CheckedFunction1<T, Try<? extends U>>) mapper::apply);
    }

    @SuppressWarnings("unchecked")
    default <U> Try<U> flatMapTry(CheckedFunction1<? super T, ? extends Try<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isFailure()) {
            return (Failure<U>) this;
        } else {
            try {
                return (Try<U>) mapper.apply(get());
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }
    }

    @Override
    T get();

    Throwable getCause();

    @Override
    boolean isEmpty();

    boolean isFailure();

    boolean isSuccess();

    @Override
    default Iterator<T> iterator() {
        return isSuccess() ? Iterators.of(get()) : Iterators.empty();
    }

    @Override
    default <U> Try<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return mapTry(mapper::apply);
    }

    default Try<T> mapFailure(Function<Throwable, ? extends Throwable> mapper) {
        if (isSuccess()) return this;
        else return failure(mapper.apply(getCause()));
    }

    @SuppressWarnings("unchecked")
    default <U> Try<U> mapTry(CheckedFunction1<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isFailure()) {
            return (Failure<U>) this;
        } else {
            try {
                return new Success<>(mapper.apply(get()));
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        }
    }

    default Try<T> onFailure(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action is null");
        if (isFailure()) {
            action.accept(getCause());
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    default <X extends Throwable> Try<T> onFailure(Class<X> exceptionType, Consumer<? super X> action) {
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        Objects.requireNonNull(action, "action is null");
        if (isFailure() && exceptionType.isAssignableFrom(getCause().getClass())) {
            action.accept((X) getCause());
        }
        return this;
    }

    default Try<T> onSuccess(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (isSuccess()) {
            action.accept(get());
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    default Try<T> orElse(Try<? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return isSuccess() ? this : (Try<T>) other;
    }

    @SuppressWarnings("unchecked")
    default Try<T> orElse(Supplier<? extends Try<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isSuccess() ? this : (Try<T>) supplier.get();
    }

    default T getOrElseGet(Function<? super Throwable, ? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        if (isFailure()) {
            return other.apply(getCause());
        } else {
            return get();
        }
    }

    default void orElseRun(Consumer<? super Throwable> action) {
        Objects.requireNonNull(action, "action is null");
        if (isFailure()) {
            action.accept(getCause());
        }
    }

    default <X extends Throwable> T getOrElseThrow(Function<? super Throwable, X> exceptionProvider) throws X {
        Objects.requireNonNull(exceptionProvider, "exceptionProvider is null");
        if (isFailure()) {
            throw exceptionProvider.apply(getCause());
        } else {
            return get();
        }
    }

    default <X> X fold(Function<? super Throwable, ? extends X> ifFail, Function<? super T, ? extends X> f) {
        if (isFailure()) {
            return ifFail.apply(getCause());
        } else {
            return f.apply(get());
        }
    }

    default Try<T> peek(Consumer<? super Throwable> failureAction, Consumer<? super T> successAction) {
        Objects.requireNonNull(failureAction, "failureAction is null");
        Objects.requireNonNull(successAction, "successAction is null");

        if (isFailure()) {
            failureAction.accept(getCause());
        } else {
            successAction.accept(get());
        }

        return this;
    }

    @Override
    default Try<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (isSuccess()) {
            action.accept(get());
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    default <X extends Throwable> Try<T> recover(Class<X> exceptionType, Function<? super X, ? extends T> f) {
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        Objects.requireNonNull(f, "f is null");
        if (isFailure()) {
            final Throwable cause = getCause();
            if (exceptionType.isAssignableFrom(cause.getClass())) {
                return Try.of(() -> f.apply((X) cause));
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    default <X extends Throwable> Try<T> recoverWith(Class<X> exceptionType, Function<? super X, Try<? extends T>> f) {
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        Objects.requireNonNull(f, "f is null");
        if (isFailure()) {
            final Throwable cause = getCause();
            if (exceptionType.isAssignableFrom(cause.getClass())) {
                try {
                    return narrow(f.apply((X) cause));
                } catch (Throwable t) {
                    return new Failure<>(t);
                }
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private static <T> Try<T> narrow(Try<? extends T> t) {
        return (Try<T>) t;
    }

    default <X extends Throwable> Try<T> recoverWith(Class<X> exceptionType, Try<? extends T> recovered) {
        Objects.requireNonNull(exceptionType, "exeptionType is null");
        Objects.requireNonNull(recovered, "recovered is null");
        return (isFailure() && exceptionType.isAssignableFrom(getCause().getClass()))
                ? narrow(recovered)
                : this;
    }

    default <X extends Throwable> Try<T> recover(Class<X> exceptionType, T value) {
        Objects.requireNonNull(exceptionType, "exceptionType is null");
        return (isFailure() && exceptionType.isAssignableFrom(getCause().getClass()))
                ? Try.success(value)
                : this;
    }

    default Try<T> recover(Function<? super Throwable, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        if (isFailure()) {
            return Try.of(() -> f.apply(getCause()));
        } else {
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    default Try<T> recoverWith(Function<? super Throwable, ? extends Try<? extends T>> f) {
        Objects.requireNonNull(f, "f is null");
        if (isFailure()) {
            try {
                return (Try<T>) f.apply(getCause());
            } catch (Throwable t) {
                return new Failure<>(t);
            }
        } else {
            return this;
        }
    }

    default Either<Throwable, T> toEither() {
        if (isFailure()) {
            return Either.left(getCause());
        } else {
            return Either.right(get());
        }
    }

    default Validation<Throwable, T> toValidation() {
        return toValidation(Function.identity());
    }

    default <U> Validation<U, T> toValidation(Function<? super Throwable, ? extends U> throwableMapper) {
        Objects.requireNonNull(throwableMapper, "throwableMapper is null");
        if (isFailure()) {
            return Validation.invalid(throwableMapper.apply(getCause()));
        } else {
            return Validation.valid(get());
        }
    }

    default <U> U transform(Function<? super Try<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    default Try<T> andFinally(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        return andFinallyTry(runnable::run);
    }

    default Try<T> andFinallyTry(CheckedRunnable runnable) {
        Objects.requireNonNull(runnable, "runnable is null");
        try {
            runnable.run();
            return this;
        } catch (Throwable t) {
            return new Failure<>(t);
        }
    }

    static <T1 extends AutoCloseable> WithResources1<T1> withResources(CheckedFunction0<? extends T1> t1Supplier) {
        return new WithResources1<>(t1Supplier);
    }

    static <T1 extends AutoCloseable, T2 extends AutoCloseable> WithResources2<T1, T2> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier) {
        return new WithResources2<>(t1Supplier, t2Supplier);
    }

    static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable> WithResources3<T1, T2, T3> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier) {
        return new WithResources3<>(t1Supplier, t2Supplier, t3Supplier);
    }

    static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable> WithResources4<T1, T2, T3, T4> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier) {
        return new WithResources4<>(t1Supplier, t2Supplier, t3Supplier, t4Supplier);
    }

    static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable> WithResources5<T1, T2, T3, T4, T5> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier) {
        return new WithResources5<>(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier);
    }

    static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable> WithResources6<T1, T2, T3, T4, T5, T6> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier) {
        return new WithResources6<>(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier, t6Supplier);
    }

    static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable> WithResources7<T1, T2, T3, T4, T5, T6, T7> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier) {
        return new WithResources7<>(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier, t6Supplier, t7Supplier);
    }

    static <T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable, T8 extends AutoCloseable> WithResources8<T1, T2, T3, T4, T5, T6, T7, T8> withResources(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier, CheckedFunction0<? extends T8> t8Supplier) {
        return new WithResources8<>(t1Supplier, t2Supplier, t3Supplier, t4Supplier, t5Supplier, t6Supplier, t7Supplier, t8Supplier);
    }

    final class WithResources1<T1 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;

        private WithResources1(CheckedFunction0<? extends T1> t1Supplier) {
            this.t1Supplier = t1Supplier;
        }

        @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
        public <R> Try<R> of(CheckedFunction1<? super T1, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply()) {
                    return f.apply(t1);
                }
            });
        }
    }

    final class WithResources2<T1 extends AutoCloseable, T2 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;

        private WithResources2(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
        }

        @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
        public <R> Try<R> of(CheckedFunction2<? super T1, ? super T2, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply(); T2 t2 = t2Supplier.apply()) {
                    return f.apply(t1, t2);
                }
            });
        }
    }

    final class WithResources3<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;

        private WithResources3(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
        }

        @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
        public <R> Try<R> of(CheckedFunction3<? super T1, ? super T2, ? super T3, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply(); T2 t2 = t2Supplier.apply(); T3 t3 = t3Supplier.apply()) {
                    return f.apply(t1, t2, t3);
                }
            });
        }
    }

    final class WithResources4<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;

        private WithResources4(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
        }

        @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
        public <R> Try<R> of(CheckedFunction4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply(); T2 t2 = t2Supplier.apply(); T3 t3 = t3Supplier.apply(); T4 t4 = t4Supplier.apply()) {
                    return f.apply(t1, t2, t3, t4);
                }
            });
        }
    }

    final class WithResources5<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;

        private WithResources5(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
        }

        @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
        public <R> Try<R> of(CheckedFunction5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply(); T2 t2 = t2Supplier.apply(); T3 t3 = t3Supplier.apply(); T4 t4 = t4Supplier.apply(); T5 t5 = t5Supplier.apply()) {
                    return f.apply(t1, t2, t3, t4, t5);
                }
            });
        }
    }

    final class WithResources6<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;
        private final CheckedFunction0<? extends T6> t6Supplier;

        private WithResources6(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
            this.t6Supplier = t6Supplier;
        }

        @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
        public <R> Try<R> of(CheckedFunction6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply(); T2 t2 = t2Supplier.apply(); T3 t3 = t3Supplier.apply(); T4 t4 = t4Supplier.apply(); T5 t5 = t5Supplier.apply(); T6 t6 = t6Supplier.apply()) {
                    return f.apply(t1, t2, t3, t4, t5, t6);
                }
            });
        }
    }

    final class WithResources7<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;
        private final CheckedFunction0<? extends T6> t6Supplier;
        private final CheckedFunction0<? extends T7> t7Supplier;

        private WithResources7(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
            this.t6Supplier = t6Supplier;
            this.t7Supplier = t7Supplier;
        }

        @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
        public <R> Try<R> of(CheckedFunction7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply(); T2 t2 = t2Supplier.apply(); T3 t3 = t3Supplier.apply(); T4 t4 = t4Supplier.apply(); T5 t5 = t5Supplier.apply(); T6 t6 = t6Supplier.apply(); T7 t7 = t7Supplier.apply()) {
                    return f.apply(t1, t2, t3, t4, t5, t6, t7);
                }
            });
        }
    }

    final class WithResources8<T1 extends AutoCloseable, T2 extends AutoCloseable, T3 extends AutoCloseable, T4 extends AutoCloseable, T5 extends AutoCloseable, T6 extends AutoCloseable, T7 extends AutoCloseable, T8 extends AutoCloseable> {

        private final CheckedFunction0<? extends T1> t1Supplier;
        private final CheckedFunction0<? extends T2> t2Supplier;
        private final CheckedFunction0<? extends T3> t3Supplier;
        private final CheckedFunction0<? extends T4> t4Supplier;
        private final CheckedFunction0<? extends T5> t5Supplier;
        private final CheckedFunction0<? extends T6> t6Supplier;
        private final CheckedFunction0<? extends T7> t7Supplier;
        private final CheckedFunction0<? extends T8> t8Supplier;

        private WithResources8(CheckedFunction0<? extends T1> t1Supplier, CheckedFunction0<? extends T2> t2Supplier, CheckedFunction0<? extends T3> t3Supplier, CheckedFunction0<? extends T4> t4Supplier, CheckedFunction0<? extends T5> t5Supplier, CheckedFunction0<? extends T6> t6Supplier, CheckedFunction0<? extends T7> t7Supplier, CheckedFunction0<? extends T8> t8Supplier) {
            this.t1Supplier = t1Supplier;
            this.t2Supplier = t2Supplier;
            this.t3Supplier = t3Supplier;
            this.t4Supplier = t4Supplier;
            this.t5Supplier = t5Supplier;
            this.t6Supplier = t6Supplier;
            this.t7Supplier = t7Supplier;
            this.t8Supplier = t8Supplier;
        }

        @SuppressWarnings("try"/* https://bugs.openjdk.java.net/browse/JDK-8155591 */)
        public <R> Try<R> of(CheckedFunction8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> f) {
            return Try.of(() -> {
                try (T1 t1 = t1Supplier.apply(); T2 t2 = t2Supplier.apply(); T3 t3 = t3Supplier.apply(); T4 t4 = t4Supplier.apply(); T5 t5 = t5Supplier.apply(); T6 t6 = t6Supplier.apply(); T7 t7 = t7Supplier.apply(); T8 t8 = t8Supplier.apply()) {
                    return f.apply(t1, t2, t3, t4, t5, t6, t7, t8);
                }
            });
        }
    }
}

final class LabelTryExtractor implements TryExtractor {

    private final Label label;

    public LabelTryExtractor(Label label) {
        this.label = label;
    }

    @Override
    public <T> T value(Try<T> t) {
        if (t.isFailure()) {
            // Break flow / Short Circuit, go to boundary definition
            Boundary.breakNow(t, label);
            return null;
        } else return t.get();
    }
}


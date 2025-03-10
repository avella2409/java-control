package control;

import control.func.CheckedPredicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TryTest extends AbstractValueTest {

    private static final String OK = "ok";
    private static final String FAILURE = "failure";

    // -- AbstractValueTest

    @Override
    protected <T> Try<T> empty() {
        return Try.failure(new NoSuchElementException());
    }

    @Override
    protected <T> Try<T> of(T element) {
        return Try.success(element);
    }

    @SafeVarargs
    @Override
    protected final <T> Try<T> of(T... elements) {
        return of(elements[0]);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    @Override
    @Test
    public void shouldGetEmpty() {
        assertThrows(NoSuchElementException.class, () -> {

            empty().get();
        });
    }

    // -- Try

    // -- andFinally

    @Test
    public void shouldExecuteAndFinallyOnSuccess() {
        final AtomicInteger count = new AtomicInteger();
        Try.run(() -> count.set(0)).andFinally(() -> count.set(1));
        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    public void shouldExecuteAndFinallyTryOnSuccess() {
        final AtomicInteger count = new AtomicInteger();
        Try.run(() -> count.set(0)).andFinallyTry(() -> count.set(1));
        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    public void shouldExecuteAndFinallyOnFailure() {
        final AtomicInteger count = new AtomicInteger();
        Try.run(() -> {
                    throw new IllegalStateException(FAILURE);
                })
                .andFinallyTry(() -> count.set(1));
        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    public void shouldExecuteAndFinallyTryOnFailure() {
        final AtomicInteger count = new AtomicInteger();
        Try.run(() -> {
                    throw new IllegalStateException(FAILURE);
                })
                .andFinallyTry(() -> count.set(1));
        assertThat(count.get()).isEqualTo(1);
    }

    @Test
    public void shouldExecuteAndFinallyTryOnFailureWithFailure() {
        final Try<Object> result = Try.of(() -> {
                    throw new IllegalStateException(FAILURE);
                })
                .andFinallyTry(() -> {
                    throw new IllegalStateException(FAILURE);
                });
        assertThat(result.isFailure());
    }

    // -- orElse

    @Test
    public void shouldReturnSelfOnOrElseIfSuccess() {
        final Try<Integer> success = Try.success(42);
        assertThat(success.orElse(Try.success(0))).isSameAs(success);
    }

    @Test
    public void shouldReturnSelfOnOrElseSupplierIfSuccess() {
        final Try<Integer> success = Try.success(42);
        assertThat(success.orElse(() -> Try.success(0))).isSameAs(success);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseIfFailure() {
        final Try<Integer> success = Try.success(42);
        assertThat(Try.failure(new RuntimeException()).orElse(success)).isSameAs(success);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseSupplierIfFailure() {
        final Try<Integer> success = Try.success(42);
        assertThat(Try.failure(new RuntimeException()).orElse(() -> success)).isSameAs(success);
    }

    // -- iterator

    @Test
    public void shouldReturnIteratorOfSuccess() {
        assertThat((Iterator<Integer>) Try.success(1).iterator()).isNotNull();
    }

    @Test
    public void shouldReturnIteratorOfFailure() {
        assertThat((Iterator<Object>) failure().iterator()).isNotNull();
    }

    // -- Try.of

    @Test
    public void shouldCreateSuccessWhenCallingTryOfCheckedFunction0() {
        assertThat(Try.of(() -> 1) instanceof Success).isTrue();
    }

    @Test
    public void shouldCreateFailureWhenCallingTryOfCheckedFunction0() {
        assertThat(Try.of(() -> {
            throw new Error("error");
        }) instanceof Failure).isTrue();
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenCallingTryOfCheckedFunction0() {
        assertThatThrownBy(() -> Try.of(null)).isInstanceOf(NullPointerException.class).hasMessage("supplier is null");
    }

    // -- Try.fold

    @Test
    public void shouldReturnValueIfSuccess() {
        final Try<Integer> success = Try.success(42);
        assertThat(success.fold(t -> {
            throw new AssertionError("Not expected to be called");
        }, Function.identity())).isEqualTo(42);
    }

    @Test
    public void shouldReturnAlternateValueIfFailure() {
        final Try<Integer> success = Try.failure(new NullPointerException("something was null"));
        assertThat(success.<Integer>fold(t -> 42, a -> {
            throw new AssertionError("Not expected to be called");
        })).isEqualTo(42);
    }

    // -- Try.ofSupplier

    @Test
    public void shouldCreateSuccessWhenCallingTryOfSupplier() {
        assertThat(Try.ofSupplier(() -> 1) instanceof Success).isTrue();
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenCallingTryOfSupplier() {
        assertThatThrownBy(() -> Try.ofSupplier(null)).isInstanceOf(NullPointerException.class).hasMessage("supplier is null");
    }

    @Test
    public void shouldCreateFailureWhenCallingTryOfSupplier() {
        assertThat(Try.ofSupplier(() -> {
            throw new Error("error");
        }) instanceof Failure).isTrue();
    }

    // -- Try.ofCallable

    @Test
    public void shouldCreateSuccessWhenCallingTryOfCallable() {
        assertThat(Try.ofCallable(() -> 1) instanceof Success).isTrue();
    }

    @Test
    public void shouldCreateFailureWhenCallingTryOfCallable() {
        assertThat(Try.ofCallable(() -> {
            throw new Error("error");
        }) instanceof Failure).isTrue();
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenCallingTryOfCallable() {
        assertThatThrownBy(() -> Try.ofCallable(null)).isInstanceOf(NullPointerException.class).hasMessage("callable is null");
    }

    // -- Try.run

    @Test
    public void shouldCreateSuccessWhenCallingTryRunCheckedRunnable() {
        assertThat(Try.run(() -> {
        }) instanceof Success).isTrue();
    }

    @Test
    public void shouldCreateFailureWhenCallingTryRunCheckedRunnable() {
        assertThat(Try.run(() -> {
            throw new Error("error");
        }) instanceof Failure).isTrue();
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenCallingTryRunCheckedRunnable() {
        assertThatThrownBy(() -> Try.run(null)).isInstanceOf(NullPointerException.class).hasMessage("runnable is null");
    }

    // -- Try.runRunnable

    @Test
    public void shouldCreateSuccessWhenCallingTryRunRunnable() {
        assertThat(Try.runRunnable(() -> {
        }) instanceof Success).isTrue();
    }

    @Test
    public void shouldCreateFailureWhenCallingTryRunRunnable() {
        assertThat(Try.runRunnable(() -> {
            throw new Error("error");
        }) instanceof Failure).isTrue();
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenCallingTryRunRunnable() {
        assertThatThrownBy(() -> Try.runRunnable(null)).isInstanceOf(NullPointerException.class).hasMessage("runnable is null");
    }

    // -- Try.withResources

    @SuppressWarnings("try")/* https://bugs.openjdk.java.net/browse/JDK-8155591 */
    static class Closeable<T> implements AutoCloseable {

        final T value;
        boolean isClosed = false;

        static <T> Closeable<T> of(T value) {
            return new Closeable<>(value);
        }

        Closeable(T value) {
            this.value = value;
        }

        @Override
        public void close() {
            isClosed = true;
        }
    }

    @Test
    public void shouldCreateSuccessTryWithResources1() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Try<String> actual = Try.withResources(() -> closeable1).of(i1 -> "" + i1.value);
        assertThat(actual).isEqualTo(Try.success("1"));
        assertThat(closeable1.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources1() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Try<?> actual = Try.withResources(() -> closeable1).of(i -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
    }

    @Test
    public void shouldCreateSuccessTryWithResources2() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Try<String> actual = Try.withResources(() -> closeable1, () -> closeable2).of((i1, i2) -> "" + i1.value + i2.value);
        assertThat(actual).isEqualTo(Try.success("12"));
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources2() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Try<?> actual = Try.withResources(() -> closeable1, () -> closeable2).of((i1, i2) -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
    }

    @Test
    public void shouldCreateSuccessTryWithResources3() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Try<String> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3).of((i1, i2, i3) -> "" + i1.value + i2.value + i3.value);
        assertThat(actual).isEqualTo(Try.success("123"));
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources3() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Try<?> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3).of((i1, i2, i3) -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
    }

    @Test
    public void shouldCreateSuccessTryWithResources4() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Try<String> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4).of((i1, i2, i3, i4) -> "" + i1.value + i2.value + i3.value + i4.value);
        assertThat(actual).isEqualTo(Try.success("1234"));
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources4() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Try<?> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4).of((i1, i2, i3, i4) -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
    }

    @Test
    public void shouldCreateSuccessTryWithResources5() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Try<String> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5).of((i1, i2, i3, i4, i5) -> "" + i1.value + i2.value + i3.value + i4.value + i5.value);
        assertThat(actual).isEqualTo(Try.success("12345"));
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources5() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Try<?> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5).of((i1, i2, i3, i4, i5) -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
    }

    @Test
    public void shouldCreateSuccessTryWithResources6() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Closeable<Integer> closeable6 = Closeable.of(6);
        final Try<String> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5, () -> closeable6).of((i1, i2, i3, i4, i5, i6) -> "" + i1.value + i2.value + i3.value + i4.value + i5.value + i6.value);
        assertThat(actual).isEqualTo(Try.success("123456"));
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
        assertThat(closeable6.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources6() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Closeable<Integer> closeable6 = Closeable.of(6);
        final Try<?> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5, () -> closeable6).of((i1, i2, i3, i4, i5, i6) -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
        assertThat(closeable6.isClosed).isTrue();
    }

    @Test
    public void shouldCreateSuccessTryWithResources7() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Closeable<Integer> closeable6 = Closeable.of(6);
        final Closeable<Integer> closeable7 = Closeable.of(7);
        final Try<String> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5, () -> closeable6, () -> closeable7).of((i1, i2, i3, i4, i5, i6, i7) -> "" + i1.value + i2.value + i3.value + i4.value + i5.value + i6.value + i7.value);
        assertThat(actual).isEqualTo(Try.success("1234567"));
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
        assertThat(closeable6.isClosed).isTrue();
        assertThat(closeable7.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources7() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Closeable<Integer> closeable6 = Closeable.of(6);
        final Closeable<Integer> closeable7 = Closeable.of(7);
        final Try<?> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5, () -> closeable6, () -> closeable7).of((i1, i2, i3, i4, i5, i6, i7) -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
        assertThat(closeable6.isClosed).isTrue();
        assertThat(closeable7.isClosed).isTrue();
    }

    @Test
    public void shouldCreateSuccessTryWithResources8() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Closeable<Integer> closeable6 = Closeable.of(6);
        final Closeable<Integer> closeable7 = Closeable.of(7);
        final Closeable<Integer> closeable8 = Closeable.of(8);
        final Try<String> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5, () -> closeable6, () -> closeable7, () -> closeable8).of((i1, i2, i3, i4, i5, i6, i7, i8) -> "" + i1.value + i2.value + i3.value + i4.value + i5.value + i6.value + i7.value + i8.value);
        assertThat(actual).isEqualTo(Try.success("12345678"));
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
        assertThat(closeable6.isClosed).isTrue();
        assertThat(closeable7.isClosed).isTrue();
        assertThat(closeable8.isClosed).isTrue();
    }

    @Test
    public void shouldCreateFailureTryWithResources8() {
        final Closeable<Integer> closeable1 = Closeable.of(1);
        final Closeable<Integer> closeable2 = Closeable.of(2);
        final Closeable<Integer> closeable3 = Closeable.of(3);
        final Closeable<Integer> closeable4 = Closeable.of(4);
        final Closeable<Integer> closeable5 = Closeable.of(5);
        final Closeable<Integer> closeable6 = Closeable.of(6);
        final Closeable<Integer> closeable7 = Closeable.of(7);
        final Closeable<Integer> closeable8 = Closeable.of(8);
        final Try<?> actual = Try.withResources(() -> closeable1, () -> closeable2, () -> closeable3, () -> closeable4, () -> closeable5, () -> closeable6, () -> closeable7, () -> closeable8).of((i1, i2, i3, i4, i5, i6, i7, i8) -> {
            throw new Error();
        });
        assertThat(actual.isFailure()).isTrue();
        assertThat(closeable1.isClosed).isTrue();
        assertThat(closeable2.isClosed).isTrue();
        assertThat(closeable3.isClosed).isTrue();
        assertThat(closeable4.isClosed).isTrue();
        assertThat(closeable5.isClosed).isTrue();
        assertThat(closeable6.isClosed).isTrue();
        assertThat(closeable7.isClosed).isTrue();
        assertThat(closeable8.isClosed).isTrue();
    }

    // -- Failure.Cause

    @Test
    public void shouldRethrowInterruptedException() {
        assertThrows(InterruptedException.class, () -> {

            Try.failure(new InterruptedException());
        });
    }

    @Test
    public void shouldRethrowOutOfMemoryError() {
        assertThrows(OutOfMemoryError.class, () -> {

            Try.failure(new OutOfMemoryError());
        });
    }

    @Test
    public void shouldDetectNonFatalException() {
        final Exception exception = new Exception();
        assertThat(Try.failure(exception).getCause()).isSameAs(exception);
    }

    @Test
    public void shouldSubsequentlyHandOverCause() {
        final Supplier<?> inner = () -> {
            throw new UnknownError("\uD83D\uDCA9");
        };
        final Supplier<?> outer = () -> Try.of(inner::get).get();
        try {
            Try.of(outer::get).get();
            Assertions.fail("Exception expected");
        } catch (UnknownError x) {
            assertThat(x.getMessage()).isEqualTo("\uD83D\uDCA9");
        } catch (Throwable x) {
            Assertions.fail("Unexpected exception type: " + x.getClass().getName());
        }
    }

    @Test
    public void shouldCreateFailureOnNonFatalException() {
        assertThat(failure().failed().get().getClass().getName()).isEqualTo(RuntimeException.class.getName());
    }

    // -- Failure.NonFatal

    @Test
    public void shouldReturnAndNotThrowOnNonFatal() {
        assertThat(Try.failure(new Exception())).isNotNull();
    }

    // -- Failure.Fatal

    @Test
    public void shouldReturnToStringOnFatal() {
        try {
            Try.of(() -> {
                throw new UnknownError("test");
            });
            Assertions.fail("Exception Expected");
        } catch (UnknownError x) {
            assertThat(x.getMessage()).isEqualTo("test");
        }
    }

    @Test
    public void shouldReturnEqualsOnFatal() {
        UnknownError error = new UnknownError();
        try {
            Try.of(() -> {
                throw error;
            });
            Assertions.fail("Exception Expected");
        } catch (UnknownError x) {
            try {
                Try.of(() -> {
                    throw error;
                });
                Assertions.fail("Exception Expected");
            } catch (UnknownError fatal) {
                assertThat(x.equals(fatal)).isEqualTo(true);
            }
        }
    }

    // -- Failure

    @Test
    public void shouldDetectFailureOfRunnable() {
        assertThat(Try.of(() -> {
            throw new RuntimeException();
        }).isFailure()).isTrue();
    }

    @Test
    public void shouldPassThroughFatalException() {
        assertThrows(UnknownError.class, () -> {
            Try.of(() -> {
                throw new UnknownError();
            });
        });

    }

    // -- isFailure

    @Test
    public void shouldDetectFailureOnNonFatalException() {
        assertThat(failure().isFailure()).isTrue();
    }

    // -- isSuccess

    @Test
    public void shouldDetectNonSuccessOnFailure() {
        assertThat(failure().isSuccess()).isFalse();
    }

    // -- get

    @Test
    public void shouldThrowWhenGetOnFailure() {
        assertThrows(RuntimeException.class, () -> {

            failure().get();
        });
    }

    @Test
    public void shouldThrowUndeclaredThrowableExceptionWhenUsingDynamicProxiesAndGetThrows() {
        final Control<?> testee = (Control<?>) Proxy.newProxyInstance(
                Control.class.getClassLoader(),
                new Class<?>[]{Control.class},
                (proxy, method, args) -> Try.failure(new Exception()).get());
        assertThatThrownBy(testee::get)
                .isInstanceOf(UndeclaredThrowableException.class)
                .hasCauseExactlyInstanceOf(Exception.class);
    }

    // -- getOrElse

    @Test
    public void shouldReturnElseWhenOrElseOnFailure() {
        assertThat(failure().getOrElse(OK)).isEqualTo(OK);
    }

    // -- getOrElseGet

    @Test
    public void shouldReturnElseWhenOrElseGetOnFailure() {
        assertThat(failure().getOrElseGet(x -> OK)).isEqualTo(OK);
    }

    // -- getOrElseThrow

    @Test
    public void shouldThrowOtherWhenGetOrElseThrowOnFailure() {
        assertThrows(IllegalStateException.class, () -> {

            failure().getOrElseThrow(x -> new IllegalStateException(OK));
        });
    }

    // -- orElseRun

    @Test
    public void shouldRunElseWhenOrElseRunOnFailure() {
        final String[] result = new String[1];
        failure().orElseRun(x -> result[0] = OK);
        assertThat(result[0]).isEqualTo(OK);
    }

    // -- recover(Class, Function)

    @Test
    public void shouldRecoverWhenFailureMatchesExactly() {
        final Try<String> testee = failure(RuntimeException.class);
        assertThat(testee.recover(RuntimeException.class, x -> OK).isSuccess()).isTrue();
    }

    @Test
    public void shouldRecoverWhenFailureIsAssignableFrom() {
        final Try<String> testee = failure(UnsupportedOperationException.class);
        assertThat(testee.recover(RuntimeException.class, x -> OK).isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnThisWhenRecoverDifferentTypeOfFailure() {
        final Try<String> testee = failure(RuntimeException.class);
        assertThat(testee.recover(NullPointerException.class, x -> OK)).isSameAs(testee);
    }

    @Test
    public void shouldReturnThisWhenRecoverSpecificFailureOnSuccess() {
        final Try<String> testee = success();
        assertThat(testee.recover(RuntimeException.class, x -> OK)).isSameAs(testee);
    }

    // -- recover(Class, Object)

    @Test
    public void shouldRecoverWithSuccessWhenFailureMatchesExactly() {
        final Try<String> testee = failure(RuntimeException.class);
        assertThat(testee.recover(RuntimeException.class, OK).isSuccess()).isTrue();
    }

    @Test
    public void shouldRecoverWithSuccessWhenFailureIsAssignableFrom() {
        final Try<String> testee = failure(UnsupportedOperationException.class);
        assertThat(testee.recover(RuntimeException.class, OK).isSuccess()).isTrue();
    }

    @Test
    public void shouldReturnThisWhenRecoverWithSuccessDifferentTypeOfFailure() {
        final Try<String> testee = failure(RuntimeException.class);
        assertThat(testee.recover(NullPointerException.class, OK)).isSameAs(testee);
    }

    @Test
    public void shouldReturnThisWhenRecoverWithSuccessSpecificFailureOnSuccess() {
        final Try<String> testee = success();
        assertThat(testee.recover(RuntimeException.class, OK)).isSameAs(testee);
    }

    // -- recover(Function)

    @Test
    public void shouldRecoverOnFailure() {
        assertThat(failure().recover(x -> OK).get()).isEqualTo(OK);
    }

    @Test
    public void shouldReturnThisWhenRecoverOnSuccess() {
        final Try<String> testee = success();
        assertThat(testee.recover(x -> OK)).isSameAs(testee);
    }

    // -- recoverWith(Function)

    @Test
    public void shouldRecoverWithOnFailure() {
        assertThat(TryTest.<String>failure().recoverWith(x -> success()).get()).isEqualTo(OK);
    }

    @Test
    public void shouldRecoverWithThrowingOnFailure() {
        final RuntimeException error = error();
        assertThat(failure().recoverWith(x -> {
            throw error;
        })).isEqualTo(Try.failure(error));
    }

    // -- recoverWith(Class, Function)

    @Test
    public void shouldNotTryToRecoverWhenItIsNotNeeded() {
        assertThat(Try.of(() -> OK).recoverWith(RuntimeException.class, x -> failure()).get()).isEqualTo(OK);
    }

    @Test
    public void shouldReturnExceptionWhenRecoveryWasNotSuccess() {
        final Try<?> testee = Try.of(() -> {
            throw error();
        }).recoverWith(IOException.class, x -> failure());
        assertThatThrownBy(testee::get).isInstanceOf(RuntimeException.class).hasMessage("error");
    }

    @Test
    public void shouldReturnErrorOfRecoveryWhenRecoveryFails() {
        final Error error = new Error();
        final Throwable actual = Try.failure(new IOException()).recoverWith(IOException.class, x -> {
            throw error;
        }).getCause();
        assertThat(actual).isSameAs(error);
    }

    @Test
    public void shouldReturnRecoveredValue() {
        assertThat(Try.of(() -> {
            throw error();
        }).recoverWith(RuntimeException.class, x -> success()).get()).isEqualTo(OK);
    }

    @Test
    public void shouldHandleErrorDuringRecovering() {
        final Try<?> t = Try.of(() -> {
            throw new IllegalArgumentException(OK);
        }).recoverWith(IOException.class, x -> {
            throw new IllegalStateException(FAILURE);
        });
        assertThatThrownBy(t::get).isInstanceOf(IllegalArgumentException.class);
    }

    // -- recoverWith(Class, Try)

    @Test
    public void shouldNotReturnRecoveredValueOnSuccess() {
        assertThat(Try.of(() -> OK).recoverWith(IOException.class, failure()).get()).isEqualTo(OK);
    }

    @Test
    public void shouldReturnRecoveredValueOnFailure() {
        assertThat(Try.of(() -> {
            throw new IllegalStateException(FAILURE);
        }).recoverWith(IllegalStateException.class, success()).get()).isEqualTo(OK);
    }

    @Test
    public void shouldNotRecoverFailureWhenExceptionTypeIsntAssignable() {
        final Throwable error = new IllegalStateException(FAILURE);
        assertThat(Try.of(() -> {
            throw error;
        }).recoverWith(Error.class, success()).getCause()).isSameAs(error);
    }

    // -- onFailure

    @Test
    public void shouldConsumeThrowableWhenCallingOnFailureGivenFailure() {
        final String[] result = new String[]{FAILURE};
        failure().onFailure(x -> result[0] = OK);
        assertThat(result[0]).isEqualTo(OK);
    }

    @Test
    public void shouldConsumeThrowableWhenCallingOnFailureWithMatchingExceptionTypeGivenFailure() {
        final String[] result = new String[]{FAILURE};
        failure().onFailure(RuntimeException.class, x -> result[0] = OK);
        assertThat(result[0]).isEqualTo(OK);
    }

    @Test
    public void shouldNotConsumeThrowableWhenCallingOnFailureWithNonMatchingExceptionTypeGivenFailure() {
        final String[] result = new String[]{OK};
        failure().onFailure(Error.class, x -> result[0] = FAILURE);
        assertThat(result[0]).isEqualTo(OK);
    }

    // -- transform

    @Test
    public void shouldThrowWhenTransformationIsNull() {
        assertThrows(NullPointerException.class, () -> {

            Try.success(1).transform(null);
        });
    }

    @Test
    public void shouldTransformSuccess() {
        final int actual = Try.success(1).transform(self -> self.get() - 1);
        assertThat(actual).isEqualTo(0);
    }

    @Test
    public void shouldTransformFailure() {
        final Error error = new Error();
        final Throwable actual = Try.failure(error).transform(Try::getCause);
        assertThat(actual).isSameAs(error);
    }

    // -- toOption

    @Test
    public void shouldConvertFailureToOption() {
        assertThat(failure().toOption().isPresent()).isFalse();
    }

    // -- toEither

    @Test
    public void shouldConvertFailureToEither() {
        assertThat(failure().toEither().isLeft()).isTrue();
    }

    @Test
    public void shouldConvertFailureToEitherLeft() {
        assertThat(failure().toEither("test").isLeft()).isTrue();
    }

    @Test
    public void shouldConvertFailureToEitherLeftSupplier() {
        assertThat(failure().toEither(() -> "test").isLeft()).isTrue();
    }


    // -- toValidation

    @Test
    public void shouldConvertFailureToValidation() {
        final Try<Object> failure = failure();
        final Validation<Throwable, Object> invalid = failure.toValidation();
        assertThat(invalid.getError()).isEqualTo(failure.getCause());
        assertThat(invalid.isInvalid()).isTrue();
    }

    @Test
    public void shouldConvertFailureToInvalidValidation() {
        final Try<Object> failure = failure();
        final Validation<String, Object> validation = failure.toValidation(Throwable::toString);
        assertThat(validation.getError()).isEqualTo(failure.getCause().toString());
        assertThat(validation.isInvalid()).isTrue();
    }

    // -- toValidation

    @Test
    public void shouldConvertFailureToValidationLeft() {
        assertThat(failure().toValidation("test").isInvalid()).isTrue();
    }

    @Test
    public void shouldConvertFailureToValidationLeftSupplier() {
        assertThat(failure().toValidation(() -> "test").isInvalid()).isTrue();
    }

    // -- toJavaOptional

    @Test
    public void shouldConvertFailureToJavaOptional() {
        assertThat(failure().toJavaOptional().isPresent()).isFalse();
    }

    // -- filter

    @Test
    public void shouldFilterMatchingPredicateOnFailure() {
        final Try<String> actual = failure();
        assertThat(actual.filter(s -> true)).isEqualTo(actual);
    }

    @Test
    public void shouldFilterNonMatchingPredicateOnFailure() {
        final Try<String> actual = failure();
        assertThat(actual.filter(s -> false)).isEqualTo(actual);
    }

    @Test
    public void shouldFilterWithExceptionOnFailure() {
        final Try<String> actual = failure();
        assertThat(actual.filter(this::filter)).isEqualTo(actual);
    }

    @Test
    public void shouldReturnIdentityWhenFilterOnFailure() {
        final Try<String> identity = failure();
        assertThat(identity.filter(s -> true)).isEqualTo(identity);
    }

    @Test
    public void shouldReturnIdentityWhenFilterWithErrorProviderOnFailure() {
        final Try<String> identity = failure();
        assertThat(identity.filter(s -> false, ignored -> new IllegalArgumentException())).isEqualTo(identity);
    }

    // -- filterNot

    @Test
    public void shouldFilterNotOnMatchingPredicateOnFailure() {
        final Try<String> failure = failure();
        assertThat(failure.filterNot(s -> false)).isEqualTo(failure);
    }

    @Test
    public void shouldFilterNotOnNonMatchingPredicateOnFailure() {
        final Try<String> failure = failure();
        assertThat(failure.filterNot(s -> true)).isEqualTo(failure);
    }

    @Test
    public void shouldFilterNotWithExceptionOnFailure() {
        final Try<String> failure = failure();
        assertThat(failure.filterNot(this::filter)).isEqualTo(failure);
    }

    @Test
    public void failureShouldThrowWhenFilterNotWithNullPredicate() {
        assertThatThrownBy(() -> failure().filterNot(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("predicate is null");
    }

    // -- flatMap

    @Test
    public void shouldFlatMapOnFailure() {
        final Try<String> actual = failure();
        assertThat(actual.flatMap(s -> Try.of(() -> s + "!"))).isEqualTo(actual);
    }

    @Test
    public void shouldFlatMapWithExceptionOnFailure() {
        final Try<String> actual = failure();
        assertThat(actual.flatMap(this::flatMap)).isEqualTo(actual);
    }

    // -- isEmpty

    @Test
    public void shouldForEachOnFailure() {
        final List<String> actual = new ArrayList<>();
        TryTest.<String>failure().forEach(actual::add);
        assertThat(actual.isEmpty()).isTrue();
    }

    // -- map

    @Test
    public void shouldMapOnFailure() {
        final Try<String> actual = failure();
        assertThat(actual.map(s -> s + "!")).isEqualTo(actual);
    }

    @Test
    public void shouldMapWithExceptionOnFailure() {
        final Try<String> actual = failure();
        assertThat(actual.map(this::map)).isEqualTo(actual);
    }

    @Test
    public void shouldChainSuccessWithMap() {
        final Try<Integer> actual = Try.of(() -> 100)
                .map(x -> x + 100)
                .map(x -> x + 50);

        final Try<Integer> expected = Try.success(250);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldChainFailureWithMap() {
        final Try<Integer> actual = Try.of(() -> 100)
                .map(x -> x + 100)
                .map(x -> Integer.parseInt("aaa") + x)   //Throws exception.
                .map(x -> x / 2);
        assertThat(actual.toString()).isEqualTo("Failure[cause=java.lang.NumberFormatException: For input string: \"aaa\"]");
    }

    // -- mapFailure

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMapFailureWhenSuccess() {
        final Try<Integer> testee = Try.success(1);
        final Try<Integer> actual = testee.mapFailure(err -> new Error());
        assertThat(actual).isSameAs(testee);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldMapFailureWhenFailureAndMatches() {
        final Try<Integer> testee = Try.failure(new IOException());
        final Try<Integer> actual = testee.mapFailure(err -> new Error());
        assertThat(actual.getCause()).isInstanceOf(Error.class);
    }

    // -- andThen

    @Test
    public void shouldComposeFailureWithAndThenWhenFailing() {
        final Try<Void> actual = Try.run(() -> {
            throw new Error("err1");
        }).andThen(() -> {
            throw new Error("err2");
        });
        assertThat(actual.toString()).isEqualTo("Failure[cause=java.lang.Error: err1]");
    }

    @Test
    public void shouldChainConsumableSuccessWithAndThen() {
        final Try<Integer> actual = Try.of(() -> new ArrayList<Integer>())
                .andThen(arr -> arr.add(10))
                .andThen(arr -> arr.add(30))
                .andThen(arr -> arr.add(20))
                .map(arr -> arr.get(1));

        final Try<Integer> expected = Try.success(30);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldChainConsumableFailureWithAndThen() {
        final Try<Integer> actual = Try.of(() -> new ArrayList<Integer>())
                .andThen(arr -> arr.add(10))
                .andThen(arr -> arr.add(Integer.parseInt("aaa"))) //Throws exception.
                .andThen(arr -> arr.add(20))
                .map(arr -> arr.get(1));
        assertThat(actual.toString()).isEqualTo("Failure[cause=java.lang.NumberFormatException: For input string: \"aaa\"]");
    }

    // peek

    @Test
    public void shouldPeekFailure() {
        final List<Object> list = new ArrayList<>();

        var before = failure();
        assertThat(before.peek(list::add)).isEqualTo(before);
        assertThat(list.isEmpty()).isTrue();
    }

    // equals

    @Test
    public void shouldEqualFailureIfObjectIsSame() {
        final Try<?> failure = Try.failure(error());
        assertThat(failure).isEqualTo(failure);
    }

    @Test
    public void shouldNotEqualFailureIfObjectIsNull() {
        assertThat(Try.failure(error())).isNotNull();
    }

    @Test
    public void shouldNotEqualFailureIfObjectIsOfDifferentType() {
        assertThat(Try.failure(error()).equals(new Object())).isFalse();
    }

    // Align with scala, exceptions are not equal
    @Test
    public void shouldNotEqualFailure() {
        assertThat(Try.failure(new RuntimeException())).isNotEqualTo(Try.failure(new RuntimeException()));
    }

    @Override
    @Test
    public void shouldPeekNil() {
        var before = empty();
        var after = before.peek(t -> {
        });
        assertThat(before == after).isTrue();
    }

    @Override
    @Test
    public void shouldSerializeDeserializeEmpty() {
        Try<String> before = empty();
        Try<String> after = Serializables.deserialize(Serializables.serialize(before));
        assertThat(after.isEmpty()).isTrue();
        assertThat(after.getCause()).isInstanceOf(before.getCause().getClass());
        assertThat(after.getCause().getMessage()).isEqualTo(before.getCause().getMessage());
    }

    // toString

    @Test
    public void shouldConvertFailureToString() {
        assertThat(Try.failure(error()).toString()).isEqualTo("Failure[cause=java.lang.RuntimeException: error]");
    }

    // -- sequence

    @Test
    public void shouldConvertListOfSuccessToTryOfList() {
        final List<Try<String>> tries = Arrays.asList(Try.success("a"), Try.success("b"), Try.success("c"));
        final Try<List<String>> reducedTry = Try.sequence(tries);
        assertThat(reducedTry instanceof Success).isTrue();
        assertThat(reducedTry.get().size()).isEqualTo(3);
        assertThat(String.join("", reducedTry.get())).isEqualTo("abc");
    }

    @Test
    public void shouldConvertListOfFailureToTryOfList() {
        final Throwable t = new RuntimeException("failure");
        final List<Try<String>> tries = Arrays.asList(Try.failure(t), Try.failure(t), Try.failure(t));
        final Try<List<String>> reducedTry = Try.sequence(tries);
        assertThat(reducedTry instanceof Failure).isTrue();
    }

    @Test
    public void shouldConvertListOfMixedTryToTryOfList() {
        final Throwable t = new RuntimeException("failure");
        final List<Try<String>> tries = Arrays.asList(Try.success("a"), Try.failure(t), Try.success("c"));
        final Try<List<String>> reducedTry = Try.sequence(tries);
        assertThat(reducedTry instanceof Failure).isTrue();
    }

    // serialization

    @Test
    public void shouldSerializeDeserializeFailure() {
        final Object actual = Serializables.deserialize(Serializables.serialize(Try.failure(error())));
        assertThat(actual.toString()).isEqualTo(Try.failure(error()).toString());
    }

    // -- Success

    @Test
    public void shouldDetectSuccessOfRunnable() {
        //noinspection ResultOfMethodCallIgnored
        assertThat(Try.run(() -> String.valueOf("side-effect")).isSuccess()).isTrue();
    }

    @Test
    public void shouldDetectSuccess() {
        assertThat(success().isSuccess()).isTrue();
    }

    @Test
    public void shouldDetectNonFailureOnSuccess() {
        assertThat(success().isFailure()).isFalse();
    }

    @Test
    public void shouldGetOnSuccess() {
        assertThat(success().get()).isEqualTo(OK);
    }

    @Test
    public void shouldGetOrElseOnSuccess() {
        assertThat(success().getOrElse((String) null)).isEqualTo(OK);
    }

    @Test
    public void shouldOrElseGetOnSuccess() {
        assertThat(success().getOrElseGet(x -> null)).isEqualTo(OK);
    }

    @Test
    public void shouldOrElseRunOnSuccess() {
        final String[] result = new String[]{OK};
        success().orElseRun(x -> result[0] = FAILURE);
        assertThat(result[0]).isEqualTo(OK);
    }

    @Test
    public void shouldOrElseThrowOnSuccess() {
        assertThat(success().getOrElseThrow(x -> null)).isEqualTo(OK);
    }

    @Test
    public void shouldRecoverOnSuccess() {
        assertThat(success().recover(x -> null).get()).isEqualTo(OK);
    }

    @Test
    public void shouldRecoverWithOnSuccess() {
        assertThat(success().recoverWith(x -> null).get()).isEqualTo(OK);
    }

    @Test
    public void shouldNotConsumeThrowableWhenCallingOnFailureGivenSuccess() {
        final String[] result = new String[]{OK};
        success().onFailure(x -> result[0] = FAILURE);
        assertThat(result[0]).isEqualTo(OK);
    }

    @Test
    public void shouldConvertSuccessToOption() {
        assertThat(success().toOption().get()).isEqualTo(OK);
    }

    @Test
    public void shouldConvertSuccessToEither() {
        assertThat(success().toEither().isRight()).isTrue();
    }

    @Test
    public void shouldConvertSuccessToValidValidation() {
        assertThat(success().toValidation().isValid()).isTrue();
    }

    @Test
    public void shouldConvertSuccessToValidValidationUsingConversionWithMapper() {
        assertThat(success().toValidation(Throwable::getMessage).isValid()).isTrue();
    }

    @Test
    public void shouldConvertSuccessToJavaOptional() {
        assertThat(success().toJavaOptional().get()).isEqualTo(OK);
    }

    // -- filter

    @Test
    public void shouldFilterMatchingPredicateOnSuccess() {
        assertThat(success().filter(s -> true).get()).isEqualTo(OK);
    }

    @Test
    public void shouldFilterMatchingPredicateWithErrorProviderOnSuccess() {
        assertThat(success().filter(s -> true, s -> new IllegalArgumentException(s)).get()).isEqualTo(OK);
    }

    @Test
    public void shouldFilterNonMatchingPredicateOnSuccess() {
        final Try<?> testee = success().filter(s -> false);
        assertThatThrownBy(testee::get).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldFilterNonMatchingPredicateAndDefaultThrowableSupplierOnSuccess() {
        assertThat(success().filter(s -> false).getCause())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldFilterNonMatchingPredicateAndCustomThrowableSupplierOnSuccess() {
        assertThat(success().filter(s -> false, () -> new IllegalArgumentException()).getCause())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldUseErrorProviderWhenFilterNonMatchingPredicateOnSuccess() throws Exception {
        assertThat(success().filter(s -> false, str -> new IllegalArgumentException(str)).getCause())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFilterWithExceptionOnSuccess() {
        assertThrows(RuntimeException.class, () -> {
            success().filter(s -> {
                throw new RuntimeException("xxx");
            }).get();
        });
    }

    // -- filterNot

    @Test
    public void shouldFilterNotOnMatchingPredicateOnSuccess() {
        assertThat(success().filterNot(s -> false).get()).isEqualTo(OK);
    }

    @Test
    public void shouldFilterNotOnMatchingPredicateWithErrorProviderOnSuccess() {
        assertThat(success().filterNot(s -> false, s -> new IllegalArgumentException(s)).get()).isEqualTo(OK);
    }

    @Test
    public void shouldFilterNotOnNonMatchingPredicateOnSuccess() {
        final Try<?> success = success().filterNot(s -> true);
        assertThatThrownBy(success::get).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldFilterNotOnNonMatchingPredicateAndDefaultThrowableSupplierOnSuccess() {
        assertThat(success().filterNot(s -> true).getCause())
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldFilterNotOnNonMatchingPredicateAndCustomThrowableSupplierOnSuccess() {
        assertThat(success().filterNot(s -> true, () -> new IllegalArgumentException()).getCause())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldUseErrorProviderWhenFilterNotOnNonMatchingPredicateOnSuccess() {
        assertThat(success().filterNot(s -> true, str -> new IllegalArgumentException(str)).getCause())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldFilterNotWithExceptionOnSuccess() {
        assertThrows(RuntimeException.class, () -> {
            success().filterNot(s -> {
                throw new RuntimeException("xxx");
            }).get();
        });
    }

    @Test
    public void successShouldThrowWhenFilterNotWithNullPredicate() {
        assertThatThrownBy(() -> success().filterNot(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("predicate is null");
    }

    // -- flatMap

    @Test
    public void shouldFlatMapOnSuccess() {
        assertThat(success().flatMap(s -> Try.of(() -> s + "!")).get()).isEqualTo(OK + "!");
    }

    @Test
    public void shouldFlatMapOnIterable() {
        final Try<Integer> success = Try.success(1);
        assertThat(success().flatMap(ignored -> success)).isEqualTo(success);
    }

    @Test
    public void shouldFlatMapOnEmptyIterable() {
        final Try<Integer> failure = Try.failure(new Error());
        assertThat(success().flatMap(ignored -> failure)).isEqualTo(failure);
    }

    @Test
    public void shouldFlatMapWithExceptionOnSuccess() {
        assertThrows(RuntimeException.class, () -> {
            success().flatMap(s -> {
                throw new RuntimeException("xxx");
            }).get();
        });
    }

    @Test
    public void shouldForEachOnSuccess() {
        final List<String> actual = new ArrayList<>();
        success().forEach(actual::add);
        assertThat(actual).isEqualTo(Collections.singletonList(OK));
    }

    @Test
    public void shouldMapOnSuccess() {
        assertThat(success().map(s -> s + "!").get()).isEqualTo(OK + "!");
    }

    @Test
    public void shouldMapWithExceptionOnSuccess() {
        final Try<?> testee = success().map(s -> {
            throw new RuntimeException("xxx");
        });
        assertThatThrownBy(testee::get).isInstanceOf(RuntimeException.class).hasMessage("xxx");
    }

    @Test
    public void shouldThrowWhenCallingFailedOnSuccess() {
        final Try<?> testee = success().failed();
        assertThatThrownBy(testee::get).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldThrowWhenCallingGetCauseOnSuccess() {
        assertThrows(UnsupportedOperationException.class, () -> {

            success().getCause();
        });
    }

    @Test
    public void shouldComposeSuccessWithAndThenWhenFailing() {
        final Try<Void> actual = Try.run(() -> {
        }).andThen(() -> {
            throw new Error("failure");
        });
        assertThat(actual.toString()).isEqualTo("Failure[cause=java.lang.Error: failure]");
    }

    @Test
    public void shouldComposeSuccessWithAndThenWhenSucceeding() {
        final Try<Void> actual = Try.run(() -> {
        }).andThen(() -> {
        });
        final Try<Void> expected = Try.run(() -> {});
        assertThat(actual).isEqualTo(expected);
    }

    // peek

    @Test
    public void shouldPeekSuccess() {
        final List<Object> list = new ArrayList<>();
        assertThat(success().peek(list::add)).isEqualTo(success());
        assertThat(list.isEmpty()).isFalse();
    }

    @Test
    public void shouldPeekSuccessAndThrow() {
        assertThrows(RuntimeException.class, () -> {

            success().peek(t -> failure().get());
        });
    }

    // equals

    @Test
    public void shouldEqualSuccessIfObjectIsSame() {
        final Try<?> success = Try.success(1);
        assertThat(success).isEqualTo(success);
    }

    @Test
    public void shouldNotEqualSuccessIfObjectIsNull() {
        assertThat(Try.success(1)).isNotNull();
    }

    @Test
    public void shouldNotEqualSuccessIfObjectIsOfDifferentType() {
        assertThat(Try.success(1).equals(new Object())).isFalse();
    }

    @Test
    public void shouldEqualSuccess() {
        assertThat(Try.success(1)).isEqualTo(Try.success(1));
    }

    // hashCode

    @Test
    public void shouldHashSuccess() {
        assertThat(Try.success(1).hashCode()).isEqualTo(Objects.hashCode(1));
    }

    // toString

    @Test
    public void shouldConvertSuccessToString() {
        assertThat(Try.success(1).toString()).isEqualTo("Success[value=1]");
    }

    // serialization

    @Test
    public void shouldSerializeDeserializeSuccess() {
        final Object actual = Serializables.deserialize(Serializables.serialize(Try.success(1)));
        assertThat(actual).isEqualTo(Try.success(1));
    }

    // -- Checked Functions

    @Test
    public void shouldCreateIdentityCheckedFunction() {
        assertThat(Function.identity()).isNotNull();
    }

    @Test
    public void shouldEnsureThatIdentityCheckedFunctionReturnsIdentity() throws Throwable {
        assertThat(Function.identity().apply(1)).isEqualTo(1);
    }

    @Test
    public void shouldNegateCheckedPredicate() {
        final CheckedPredicate<Integer> greaterThanZero = i -> i > 0;
        final int num = 1;
        try {
            assertThat(greaterThanZero.test(num)).isTrue();
            assertThat(greaterThanZero.negate().test(-num)).isTrue();
        } catch (Throwable x) {
            Assertions.fail("should not throw");
        }
    }

    // -- helpers

    private RuntimeException error() {
        return new RuntimeException("error");
    }

    private static <T> Try<T> failure() {
        return Try.failure(new RuntimeException());
    }

    private static <T, X extends Throwable> Try<T> failure(Class<X> exceptionType) {
        try {
            final X exception = exceptionType.getConstructor().newInstance();
            return Try.failure(exception);
        } catch (Throwable e) {
            throw new IllegalStateException("Error instantiating " + exceptionType, e);
        }
    }

    private <T> boolean filter(T t) {
        throw new RuntimeException("xxx");
    }

    private <T> Try<T> flatMap(T t) {
        throw new RuntimeException("xxx");
    }

    private <T> T map(T t) {
        throw new RuntimeException("xxx");
    }

    private Try<String> success() {
        return Try.of(() -> "ok");
    }

    // -- spliterator

    @Test
    public void shouldHaveSizedSpliterator() {
        assertThat(of(1).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isTrue();
    }

    @Test
    public void shouldHaveOrderedSpliterator() {
        assertThat(of(1).spliterator().hasCharacteristics(Spliterator.ORDERED)).isTrue();
    }

    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(of(1).spliterator().getExactSizeIfKnown()).isEqualTo(1);
    }
}

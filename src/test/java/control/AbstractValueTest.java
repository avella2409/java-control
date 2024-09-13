package control;

import org.assertj.core.api.*;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractValueTest {

    protected Random getRandom(int seed) {
        if (seed >= 0) {
            return new Random(seed);
        } else {
            final Random random = new Random();
            seed = random.nextInt();
            System.out.println("using seed: " + seed);
            random.setSeed(seed);
            return random;
        }
    }

    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
        };
    }

    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {
        };
    }

    protected <T> ObjectArrayAssert<T> assertThat(T[] actual) {
        return new ObjectArrayAssert<T>(actual) {
        };
    }

    protected BooleanAssert assertThat(Boolean actual) {
        return new BooleanAssert(actual) {
        };
    }

    protected DoubleAssert assertThat(Double actual) {
        return new DoubleAssert(actual) {
        };
    }

    protected IntegerAssert assertThat(Integer actual) {
        return new IntegerAssert(actual) {
        };
    }

    protected LongAssert assertThat(Long actual) {
        return new LongAssert(actual) {
        };
    }

    protected StringAssert assertThat(String actual) {
        return new StringAssert(actual) {
        };
    }

    abstract protected <T> Control<T> empty();

    abstract protected <T> Control<T> of(T element);

    @SuppressWarnings("unchecked")
    abstract protected <T> Control<T> of(T... elements);

    // TODO: Eliminate this method. Switching the behavior of unit tests is evil. Tests should not contain additional logic. Also it seems currently to be used in different sematic contexts.
    abstract protected boolean useIsEqualToInsteadOfIsSameAs();

    // returns the peek result of the specific Traversable implementation
    abstract protected int getPeekNonNilPerformingAnAction();

    // -- get()

    @Test
    public void shouldGetEmpty() {
        assertThrows(NoSuchElementException.class, () -> empty().get());
    }

    @Test
    public void shouldGetNonEmpty() {
        assertThat(of(1).get()).isEqualTo(1);
    }

    // -- getOrElse(T)

    @Test
    public void shouldCalculateGetOrElseWithNull() {
        assertThat(this.<Integer>empty().getOrElse((Integer) null)).isEqualTo(null);
        assertThat(of(1).getOrElse((Integer) null)).isEqualTo(1);
    }

    @Test
    public void shouldCalculateGetOrElseWithNonNull() {
        assertThat(empty().getOrElse(1)).isEqualTo(1);
        assertThat(of(1).getOrElse(2)).isEqualTo(1);
    }

    // -- getOrElse(Supplier)

    @Test
    public void shouldThrowOnGetOrElseWithNullSupplier() {
        assertThrows(NullPointerException.class, () -> {
            final Supplier<?> supplier = null;
            empty().getOrElse(supplier);
        });
    }

    @Test
    public void shouldCalculateGetOrElseWithSupplier() {
        assertThat(empty().getOrElse(() -> 1)).isEqualTo(1);
        assertThat(of(1).getOrElse(() -> 2)).isEqualTo(1);
    }

    // -- getOrElseThrow

    @Test
    public void shouldThrowOnGetOrElseThrowIfEmpty() {
        assertThrows(ArithmeticException.class, () -> empty().getOrElseThrow(ArithmeticException::new));
    }

    @Test
    public void shouldNotThrowOnGetOrElseThrowIfNonEmpty() {
        assertThat(of(1).getOrElseThrow(ArithmeticException::new)).isEqualTo(1);
    }

    // -- getOrElseTry

    @Test
    public void shouldReturnUnderlyingValueWhenCallingGetOrElseTryOnNonEmptyValue() {
        assertThat(of(1).getOrElseTry(() -> 2)).isEqualTo(1);
    }

    @Test
    public void shouldReturnAlternateValueWhenCallingGetOrElseTryOnEmptyValue() {
        assertThat(empty().getOrElseTry(() -> 2)).isEqualTo(2);
    }

    @Test
    public void shouldThrowWhenCallingGetOrElseTryOnEmptyValueAndTryIsAFailure() {
        assertThrows(Error.class, () -> empty().getOrElseTry(() -> {
            throw new Error();
        }));
    }

    // -- getOrNull

    @Test
    public void shouldReturnNullWhenGetOrNullOfEmpty() {
        assertThat(empty().getOrNull()).isEqualTo(null);
    }

    @Test
    public void shouldReturnValueWhenGetOrNullOfNonEmpty() {
        assertThat(of(1).getOrNull()).isEqualTo(1);
    }


    boolean isSingleValued() {
        return true;
    }

    // -- forEach

    @Test
    public void shouldPerformsActionOnEachElement() {
        final int[] consumer = new int[1];
        final Control<Integer> value = of(1, 2, 3);
        value.forEach(i -> consumer[0] += i);
        assertThat(consumer[0]).isEqualTo(isSingleValued() ? 1 : 6);
    }

    // -- isEmpty

    @Test
    public void shouldCalculateIsEmpty() {
        assertThat(empty().isEmpty()).isTrue();
        assertThat(of(1).isEmpty()).isFalse();
    }

    // -- peek

    @Test
    public void shouldPeekNil() {
        assertThat(empty().peek(t -> {
        })).isEqualTo(empty());
    }

    @Test
    public void shouldPeekNonNilPerformingNoAction() {
        assertThat(of(1).peek(t -> {
        })).isEqualTo(of(1));
    }

    @Test
    public void shouldPeekSingleValuePerformingAnAction() {
        final int[] effect = {0};
        final Control<Integer> actual = of(1).peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(of(1));
        assertThat(effect[0]).isEqualTo(1);
    }

    @Test
    public void shouldPeekNonNilPerformingAnAction() {
        final int[] effect = {0};
        final Control<Integer> actual = of(1, 2, 3).peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(of(1, 2, 3)); // traverses all elements in the lazy case
        assertThat(effect[0]).isEqualTo(getPeekNonNilPerformingAnAction());
    }

    @Test
    public void shouldConvertToOption() {
        assertThat(empty().toOption()).isSameAs(Option.none());
        assertThat(of(1).toOption()).isEqualTo(Option.ofNullable(1));
    }

    @Test
    public void shouldConvertToEither() {
        assertThat(empty().toEither("test")).isEqualTo(Either.left("test"));
        assertThat(empty().toEither(() -> "test")).isEqualTo(Either.left("test"));
        assertThat(of(1).toEither("test")).isEqualTo(Either.right(1));
    }

    @Test
    public void shouldConvertToValidation() {
        assertThat(empty().toValidation("test")).isEqualTo(Validation.invalid("test"));
        assertThat(empty().toValidation(() -> "test")).isEqualTo(Validation.invalid("test"));
        assertThat(of(1).toValidation("test")).isEqualTo(Validation.valid(1));
    }

    @Test
    public void shouldConvertToJavaStream() {
        final Control<Integer> value = of(1, 2, 3);
        final java.util.stream.Stream<Integer> s1 = value.stream();
        //noinspection Duplicates
        final java.util.stream.Stream<Integer> s2 = java.util.stream.Stream.of(1);

        assertThat(s1.toList()).isEqualTo(s2.toList());
    }

    @Test
    public void shouldConvertNonEmptyToTry() {
        assertThat(of(1, 2, 3).toTry()).isEqualTo(Try.of(() -> 1));
    }

    @Test
    public void shouldConvertEmptyToTry() {
        final Try<?> actual = empty().toTry();
        assertThat(actual.isFailure()).isTrue();
        assertThat(actual.getCause()).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldConvertNonEmptyToTryUsingExceptionSupplier() {
        final Exception x = new Exception("test");
        assertThat(of(1, 2, 3).toTry(() -> x)).isEqualTo(Try.of(() -> 1));
    }

    @Test
    public void shouldConvertEmptyToTryUsingExceptionSupplier() {
        final Exception x = new Exception("test");
        assertThat(empty().toTry(() -> x)).isEqualTo(Try.failure(x));
    }


    @Test
    public void shouldConvertToJavaOptional() {
        assertThat(of(1, 2, 3).toJavaOptional()).isEqualTo(Optional.of(1));
    }

    @Test
    public void shouldSerializeDeserializeEmpty() {
        final Control<?> testee = empty();
        final Control<?> actual = Serializables.deserialize(Serializables.serialize(testee));
        assertThat(actual).isEqualTo(testee);
    }

    @Test
    public void shouldSerializeDeserializeSingleValued() {
        final Control<?> testee = of(1);
        final Control<?> actual = Serializables.deserialize(Serializables.serialize(testee));
        assertThat(actual).isEqualTo(testee);
    }

    @Test
    public void shouldSerializeDeserializeMultiValued() {
        final Control<?> testee = of(1, 2, 3);
        final Control<?> actual = Serializables.deserialize(Serializables.serialize(testee));
        assertThat(actual).isEqualTo(testee);
    }

    @Test
    public void shouldPreserveSingletonInstanceOnDeserialization() {
        if (!useIsEqualToInsteadOfIsSameAs()) {
            final Control<?> empty = empty();
            final Control<?> actual = Serializables.deserialize(Serializables.serialize(empty));
            assertThat(actual).isSameAs(empty);
        }
    }

    // -- equals

    @Test
    public void shouldRecognizeSameObject() {
        final Control<Integer> v = of(1);
        //noinspection EqualsWithItself
        assertThat(v.equals(v)).isTrue();
    }

    @Test
    public void shouldRecognizeEqualObjects() {
        final Control<Integer> v1 = of(1);
        final Control<Integer> v2 = of(1);
        assertThat(v1.equals(v2)).isTrue();
    }

    @Test
    public void shouldRecognizeUnequalObjects() {
        final Control<Integer> v1 = of(1);
        final Control<Integer> v2 = of(2);
        assertThat(v1.equals(v2)).isFalse();
    }

}

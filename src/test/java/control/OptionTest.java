package control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OptionTest extends AbstractValueTest {

    // -- AbstractValueTest

    @Override
    protected <T> Option<T> empty() {
        return Option.none();
    }

    @Override
    protected <T> Option<T> of(T element) {
        return Option.some(element);
    }

    @SafeVarargs
    @Override
    protected final <T> Option<T> of(T... elements) {
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

    // -- Option


    // -- construction

    @Test
    public void shouldMapNullToNone() {
        assertThat(Option.ofNullable(null)).isEqualTo(Option.none());
    }

    @Test
    public void shouldMapNonNullToSome() {
        final Option<?> option = Option.ofNullable(new Object());
        assertThat(option.isPresent()).isTrue();
    }

    @Test
    public void shouldNotBeAbleToWrapNullInSome() {
        assertThrows(NullPointerException.class, () -> {
            final Option<?> some = Option.some(null);
        });
    }

    @Test
    public void shouldWrapIfTrue() {
        assertThat(Option.when(true, () -> "value")).isEqualTo(Option.some("value"));
        assertThat(Option.when(true, "value")).isEqualTo(Option.some("value"));
    }

    @Test
    public void shouldNotWrapIfFalse() {
        assertThat(Option.when(false, () -> null)).isEqualTo(Option.none());
        assertThat(Option.when(false, (Object) null)).isEqualTo(Option.none());
    }

    @Test
    public void shouldNotExecuteIfFalse() {
        assertThat(Option.when(false, () -> {
            throw new RuntimeException();
        })).isEqualTo(Option.none());
    }

    @Test
    public void shouldThrowExceptionOnWhenWithProvider() {
        assertThrows(NullPointerException.class, () -> {

            assertThat(Option.when(false, (Supplier<?>) null)).isEqualTo(Option.none());
        });
    }

    @Test
    public void shouldWrapEmptyOptional() {
        assertThat(Option.ofOptional(Optional.empty())).isEqualTo(Option.none());
    }

    @Test
    public void shouldWrapSomeOptional() {
        assertThat(Option.ofOptional(Optional.of(1))).isEqualTo(Option.ofNullable(1));
    }

    @Test
    public void shouldThrowExceptionOnNullOptional() {
        assertThrows(NullPointerException.class, () -> {
            assertThat(Option.ofOptional(null)).isEqualTo(Option.none());
        });
    }

    // -- sequence

    @Test
    public void shouldConvertListOfNonEmptyOptionsToOptionOfList() {
        final List<Option<String>> options = Arrays.asList(Option.ofNullable("a"), Option.ofNullable("b"), Option.ofNullable("c"));
        final Option<List<String>> reducedOption = Option.sequence(options);
        assertThat(reducedOption instanceof Some).isTrue();
        assertThat(reducedOption.get().size()).isEqualTo(3);
        assertThat(String.join("", reducedOption.get())).isEqualTo("abc");
    }

    @Test
    public void shouldConvertListOfEmptyOptionsToOptionOfList() {
        final List<Option<String>> options = Arrays.asList(Option.none(), Option.none(), Option.none());
        final Option<List<String>> option = Option.sequence(options);
        assertThat(option instanceof None).isTrue();
    }

    @Test
    public void shouldConvertListOfMixedOptionsToOptionOfList() {
        final List<Option<String>> options = Arrays.asList(Option.ofNullable("a"), Option.none(), Option.ofNullable("c"));
        final Option<List<String>> option = Option.sequence(options);
        assertThat(option instanceof None).isTrue();
    }

    // -- get

    @Test
    public void shouldSucceedOnGetWhenValueIsPresent() {
        assertThat(Option.ofNullable(1).get()).isEqualTo(1);
    }

    @Test
    public void shouldThrowOnGetWhenValueIsNotDefined() {
        assertThrows(NoSuchElementException.class, () -> {
            Option.none().get();
        });
    }

    // -- orElse

    @Test
    public void shouldReturnSelfOnOrElseIfValueIsPresent() {
        final Option<Integer> opt = Option.ofNullable(42);
        assertThat(opt.orElse(Option.ofNullable(0))).isSameAs(opt);
    }

    @Test
    public void shouldReturnSelfOnOrElseSupplierIfValueIsPresent() {
        final Option<Integer> opt = Option.ofNullable(42);
        assertThat(opt.orElse(() -> Option.ofNullable(0))).isSameAs(opt);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseIfValueIsNotDefined() {
        final Option<Integer> opt = Option.ofNullable(42);
        assertThat(Option.none().orElse(opt)).isSameAs(opt);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseSupplierIfValueIsNotDefined() {
        final Option<Integer> opt = Option.ofNullable(42);
        assertThat(Option.none().orElse(() -> opt)).isSameAs(opt);
    }

    // -- orNull

    @Test
    public void shouldReturnValueOnOrNullIfValueIsDefined() {
        assertThat(Option.ofNullable("v").orNull()).isEqualTo("v");
    }

    @Test
    public void shouldReturnValueOnOrNullIfValueIsEmpty() {
        assertThat(Option.none().orNull()).isNull();
    }

    // -- getOrElse

    @Test
    public void shouldGetValueOnGetOrElseWhenValueIsPresent() {
        assertThat(Option.ofNullable(1).getOrElse(2)).isEqualTo(1);
    }

    @Test
    public void shouldGetAlternativeOnGetOrElseWhenValueIsNotDefined() {
        assertThat(Option.none().getOrElse(2)).isEqualTo(2);
    }

    // -- getOrElse

    @Test
    public void shouldGetValueOnGetOrElseGetWhenValueIsPresent() {
        assertThat(Option.ofNullable(1).getOrElse(() -> 2)).isEqualTo(1);
    }

    @Test
    public void shouldGetAlternativeOnGetOrElseGetWhenValueIsNotDefined() {
        assertThat(Option.none().getOrElse(() -> 2)).isEqualTo(2);
    }

    // -- getOrElseThrow

    @Test
    public void shouldGetValueOnGetOrElseThrowWhenValueIsPresent() {
        assertThat(Option.ofNullable(1).getOrElseThrow(() -> new RuntimeException("none"))).isEqualTo(1);
    }

    @Test
    public void shouldThrowOnGetOrElseThrowWhenValueIsNotDefined() {
        assertThrows(RuntimeException.class, () -> {

            Option.none().getOrElseThrow(() -> new RuntimeException("none"));
        });
    }

    // -- toJavaOptional

    @Test
    public void shouldConvertNoneToJavaOptional() {
        final Option<Object> none = Option.none();
        assertThat(none.toJavaOptional()).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldConvertSomeToJavaOptional() {
        final Option<Integer> some = Option.some(1);
        assertThat(some.toJavaOptional()).isEqualTo(Optional.of(1));
    }

    // -- isDefined

    @Test
    public void shouldBePresentOnIsDefinedWhenValueIsDefined() {
        assertThat(Option.ofNullable(1).isPresent()).isTrue();
    }

    @Test
    public void shouldNotBePresentOnIsDefinedWhenValueIsNotDefined() {
        assertThat(Option.none().isPresent()).isFalse();
    }

    // -- isEmpty

    @Test
    public void shouldBeEmptyOnIsEmptyWhenValueIsEmpty() {
        assertThat(Option.none().isEmpty()).isTrue();
    }

    @Test
    public void shouldBePresentOnIsEmptyWhenValue() {
        assertThat(Option.ofNullable(1).isEmpty()).isFalse();
    }

    // -- onEmpty

    @Test
    public void shouldThrowNullPointerExceptionWhenNullOnEmptyActionPassed() {
        try {
            final Option<String> none = Option.none();
            none.onEmpty(null);
            Assertions.fail("No exception was thrown");
        } catch (NullPointerException exc) {
            assertThat(exc.getMessage()).isEqualTo("action is null");
        }
    }

    @Test
    public void shouldExecuteRunnableWhenOptionIsEmpty() {
        final AtomicBoolean state = new AtomicBoolean();
        final Option<?> option = Option.none().onEmpty(() -> state.set(false));
        assertThat(state.get()).isFalse();
        assertThat(option).isSameAs(Option.none());
    }

    @Test
    public void shouldNotThrowExceptionIfOnEmptySetAndOptionIsSome() {
        try {
            final Option<String> none = Option.some("value");
            none.onEmpty(() -> {
                throw new RuntimeException("Exception from empty option!");
            });
        } catch (RuntimeException exc) {
            Assertions.fail("No exception should be thrown!");
        }
    }

    // -- filter

    @Test
    public void shouldReturnSomeOnFilterWhenValueIsDefinedAndPredicateMatches() {
        assertThat(Option.ofNullable(1).filter(i -> i == 1)).isEqualTo(Option.ofNullable(1));
    }

    @Test
    public void shouldReturnNoneOnFilterWhenValueIsDefinedAndPredicateNotMatches() {
        assertThat(Option.ofNullable(1).filter(i -> i == 2)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneOnFilterWhenValueIsNotDefinedAndPredicateNotMatches() {
        assertThat(Option.<Integer>none().filter(i -> i == 1)).isEqualTo(Option.none());
    }

    // -- filterNot

    @Test
    public void shouldReturnSomeOnFilterNotWhenValueIsDefinedAndPredicateNotMatches() {
        assertThat(Option.ofNullable(1).filterNot(i -> i == 2)).isEqualTo(Option.ofNullable(1));
    }

    @Test
    public void shouldReturnNoneOnFilterNotWhenValuesIsDefinedAndPredicateMatches() {
        assertThat(Option.ofNullable(1).filterNot(i -> i == 1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneOnFilterNotWhenValueIsNotDefinedAndPredicateNotMatches() {
        assertThat(Option.<Integer>none().filterNot(i -> i == 1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldThrowWhenFilterNotPredicateIsNull() {
        assertThatThrownBy(() -> Option.ofNullable(1).filterNot(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("predicate is null");
    }

    // -- map

    @Test
    public void shouldMapSome() {
        assertThat(Option.ofNullable(1).map(String::valueOf)).isEqualTo(Option.ofNullable("1"));
    }

    @Test
    public void shouldMapNone() {
        assertThat(Option.<Integer>none().map(String::valueOf)).isEqualTo(Option.none());
    }

    // -- flatMap

    @Test
    public void shouldFlatMapSome() {
        assertThat(Option.ofNullable(1).flatMap(i -> Option.ofNullable(String.valueOf(i)))).isEqualTo(Option.ofNullable("1"));
    }

    @Test
    public void shouldFlatMapNone() {
        assertThat(Option.<Integer>none().flatMap(i -> Option.ofNullable(String.valueOf(i)))).isEqualTo(Option.none());
    }

    @Test
    public void shouldFlatMapNonEmptyIterable() {
        final Option<Integer> option = Option.some(2);
        assertThat(Option.ofNullable(1).flatMap(i -> option)).isEqualTo(Option.ofNullable(2));
    }

    @Test
    public void shouldFlatMapEmptyIterable() {
        final Option<Integer> option = Option.none();
        assertThat(Option.ofNullable(1).flatMap(i -> option)).isEqualTo(Option.none());
    }
    
    // -- forEach

    @Test
    public void shouldConsumePresentValueOnForEachWhenValueIsDefined() {
        final int[] actual = new int[]{-1};
        Option.ofNullable(1).forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(1);
    }

    @Test
    public void shouldNotConsumeAnythingOnForEachWhenValueIsNotDefined() {
        final int[] actual = new int[]{-1};
        Option.<Integer>none().forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(-1);
    }

    // -- toEither

    @Test
    public void shouldMakeRightOnSomeToEither() {
        assertThat(Option.some(5).toEither("bad")).isEqualTo(Either.right(5));
    }

    @Test
    public void shouldMakeLeftOnNoneToEither() {
        assertThat(Option.none().toEither("bad")).isEqualTo(Either.left("bad"));
    }

    @Test
    public void shouldMakeLeftOnNoneToEitherSupplier() {
        assertThat(Option.none().toEither(() -> "bad")).isEqualTo(Either.left("bad"));
    }

    // -- toValidation

    @Test
    public void shouldMakeValidOnSomeToValidation() {
        assertThat(Option.some(5).toValidation("bad")).isEqualTo(Validation.valid(5));
    }

    @Test
    public void shouldMakeLeftOnNoneToValidation() {
        assertThat(Option.none().toValidation("bad")).isEqualTo(Validation.invalid("bad"));
    }

    @Test
    public void shouldMakeLeftOnNoneToValidationSupplier() {
        assertThat(Option.none().toValidation(() -> "bad")).isEqualTo(Validation.invalid("bad"));
    }

    // -- peek

    @Test
    public void shouldConsumePresentValueOnPeekWhenValueIsDefined() {
        final int[] actual = new int[]{-1};
        final Option<Integer> testee = Option.ofNullable(1).peek(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(1);
        assertThat(testee).isEqualTo(Option.ofNullable(1));
    }

    @Test
    public void shouldNotConsumeAnythingOnPeekWhenValueIsNotDefined() {
        final int[] actual = new int[]{-1};
        final Option<Integer> testee = Option.<Integer>none().peek(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(-1);
        assertThat(testee).isEqualTo(Option.none());
    }

    // -- transform

    @Test
    public void shouldThrowExceptionOnNullTransformFunction() {
        assertThrows(NullPointerException.class, () -> {

            Option.some(1).transform(null);
        });
    }

    @Test
    public void shouldApplyTransformFunctionToSome() {
        final Option<Integer> option = Option.some(1);
        final Function<Option<Integer>, String> f = o -> o.get().toString().concat("-transformed");
        assertThat(option.transform(f)).isEqualTo("1-transformed");
    }

    @Test
    public void shouldHandleTransformOnNone() {
        assertThat(Option.none().<String>transform(self -> self.isEmpty() ? "ok" : "failed")).isEqualTo("ok");
    }

    // -- iterator

    @Test
    public void shouldReturnIteratorOfSome() {
        assertThat((Iterator<Integer>) Option.some(1).iterator()).isNotNull();
    }

    @Test
    public void shouldReturnIteratorOfNone() {
        assertThat((Iterator<Object>) Option.none().iterator()).isNotNull();
    }

    // -- equals

    @Test
    public void shouldEqualNoneIfObjectIsSame() {
        final Option<?> none = Option.none();
        assertThat(none).isEqualTo(none);
    }

    @Test
    public void shouldEqualSomeIfObjectIsSame() {
        final Option<?> some = Option.some(1);
        assertThat(some).isEqualTo(some);
    }

    @Test
    public void shouldNotEqualNoneIfObjectIsNull() {
        assertThat(Option.none()).isNotNull();
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsNull() {
        assertThat(Option.some(1)).isNotNull();
    }

    @Test
    public void shouldNotEqualNoneIfObjectIsOfDifferentType() {
        final Object none = Option.none();
        assertThat(none.equals(new Object())).isFalse();
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsOfDifferentType() {
        final Object some = Option.some(1);
        assertThat(some.equals(new Object())).isFalse();
    }

    @Test
    public void shouldEqualSomeIfObjectsAreEquivalent() {
        assertThat(Option.some(1)).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsOfDifferentValue() {
        assertThat(Option.some(1)).isNotEqualTo(Option.some(2));
    }

    // -- hashCode

    @Test
    public void shouldHashNone() {
        assertThat(Option.none().hashCode()).isEqualTo(0);
    }

    @Test
    public void shouldHashSome() {
        assertThat(Option.some(1).hashCode()).isEqualTo(Objects.hashCode(1));
    }

    // -- toString

    @Test
    public void shouldConvertSomeToString() {
        assertThat(Option.some(1).toString()).isEqualTo("Some[value=1]");
    }

    @Test
    public void shouldConvertNoneToString() {
        assertThat(Option.none().toString()).isEqualTo("None[]");
    }

    // -- serialization

    @Test
    public void shouldPreserveSingletonWhenDeserializingNone() {
        final Object none = Serializables.deserialize(Serializables.serialize(Option.none()));
        assertThat(none == Option.none()).isTrue();
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

    @Test
    public void foldStringToInt() {
        assertThat(Option.some("1").fold(() -> -1, Integer::valueOf)).isEqualTo(1);
        assertThat(Option.<String>none().fold(() -> -1, Integer::valueOf)).isEqualTo(-1);
    }

    @Test
    public void foldEither() {
        Either<String, Integer> right = Option.some(1).fold(() -> {
            throw new AssertionError("Must not happen");
        }, Either::right);
        Either<String, Integer> left = Option.<Integer>none().fold(() -> Either.left("Empty"), ignore -> {
            throw new AssertionError("Must not happen");
        });
        assertThat(right.get()).isEqualTo(1);
        assertThat(left.getLeft()).isEqualTo("Empty");
    }
}

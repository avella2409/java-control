package control;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationTest extends AbstractValueTest {

    private static final String OK = "ok";
    private static final List<String> ERRORS = List.of("error1", "error2", "error3");

    // -- AbstractValueTest

    @Override
    protected <T> Validation<String, T> empty() {
        return Validation.invalid("empty");
    }

    @Override
    protected <T> Validation<String, T> of(T element) {
        return Validation.valid(element);
    }

    @SafeVarargs
    @Override
    protected final <T> Validation<Object, T> of(T... elements) {
        return Validation.valid(elements[0]);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    // -- Validation.valid

    @Test
    public void shouldCreateSuccessWhenCallingValidationSuccess() {
        assertThat(Validation.valid(1) instanceof Valid).isTrue();
    }

    // -- Validation.invalid

    @Test
    public void shouldCreateFailureWhenCallingValidationFailure() {
        assertThat(Validation.invalid("error") instanceof Invalid).isTrue();
    }

    // -- Validation.fromEither

    @Test
    public void shouldCreateFromRightEither() {
        Validation<String, Integer> validation = Validation.fromEither(Either.right(42));
        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get()).isEqualTo(42);
    }

    @Test
    public void shouldCreateFromLeftEither() {
        Validation<String, Integer> validation = Validation.fromEither(Either.left("vavr"));
        assertThat(validation.isValid()).isFalse();
        assertThat(validation.getError()).isEqualTo("vavr");
    }

    // -- Validation.fromTry

    @Test
    public void shouldCreateFromSuccessTry() {
        Validation<Throwable, Integer> validation = Validation.fromTry(Try.success(42));
        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get()).isEqualTo(42);
    }

    @Test
    public void shouldCreateFromFailureTry() {
        Throwable throwable = new Throwable("vavr");
        Validation<Throwable, Integer> validation = Validation.fromTry(Try.failure(throwable));
        assertThat(validation.isValid()).isFalse();
        assertThat(validation.getError()).isEqualTo(throwable);
    }

    // -- Validation.sequence

    @Test
    public void shouldThrowWhenSequencingNull() {
        assertThrows(NullPointerException.class, () -> {

            Validation.sequence(null);
        });
    }

    @Test
    public void shouldCreateValidWhenSequencingValids() {
        final Validation<List<String>, List<Integer>> actual = Validation.sequence(List.of(
                Validation.valid(1),
                Validation.valid(2)
        ));
        assertThat(actual).isEqualTo(Validation.valid(List.of(1, 2)));
    }

    @Test
    public void shouldCreateInvalidWhenSequencingAnInvalid() {
        final Validation<List<String>, List<Integer>> actual = Validation.sequence(List.of(
                Validation.valid(1),
                Validation.invalid(List.of("error1", "error2")),
                Validation.valid(2),
                Validation.invalid(List.of("error3", "error4"))
        ));
        assertThat(actual).isEqualTo(Validation.invalid(List.of("error1", "error2", "error3", "error4")));
    }

    // -- toEither

    @Test
    public void shouldConvertToRightEither() {
        Either<?, Integer> either = Validation.valid(42).toEither();
        assertThat(either.isRight()).isTrue();
        assertThat(either.get()).isEqualTo(42);
    }

    @Test
    public void shouldConvertToLeftEither() {
        Either<String, ?> either = Validation.invalid("vavr").toEither();
        assertThat(either.isLeft()).isTrue();
        assertThat(either.getLeft()).isEqualTo("vavr");
    }

    // -- filter

    @Test
    public void shouldFilterValid() {
        Validation<String, Integer> valid = Validation.valid(42);
        assertThat(valid.filter(i -> true).get()).isSameAs(valid);
        assertThat(valid.filter(i -> false)).isSameAs(Option.none());
    }

    @Test
    public void shouldFilterInvalid() {
        Validation<String, Integer> invalid = Validation.invalid("vavr");
        assertThat(invalid.filter(i -> true).get()).isSameAs(invalid);
        assertThat(invalid.filter(i -> false).get()).isSameAs(invalid);
    }

    // -- flatMap

    @Test
    public void shouldFlatMapValid() {
        Validation<String, Integer> valid = Validation.valid(42);
        assertThat(valid.flatMap(v -> Validation.valid("ok")).get()).isEqualTo("ok");
    }

    @Test
    public void shouldFlatMapInvalid() {
        Validation<String, Integer> invalid = Validation.invalid("vavr");
        assertThat(invalid.flatMap(v -> Validation.valid("ok"))).isSameAs(invalid);
    }

    // -- peekError

    @Test
    public void shouldPeekErrorNil() {
        assertThat(empty().peekError(t -> {
        })).isEqualTo(empty());
    }

    @Test
    public void shouldPeekErrorForInvalid() {
        final int[] effect = {0};
        final Validation<Integer, ?> actual = Validation.invalid(1).peekError(i -> effect[0] = i);
        assertThat(actual).isEqualTo(Validation.invalid(1));
        assertThat(effect[0]).isEqualTo(1);
    }

    @Test
    public void shouldNotPeekErrorForValid() {
        Validation.valid(1).peekError(i -> {
            throw new IllegalStateException();
        });
    }

    // -- orElse

    @Test
    public void shouldReturnSelfOnOrElseIfValid() {
        Validation<List<String>, String> validValidation = valid();
        assertThat(validValidation.orElse(invalid())).isSameAs(validValidation);
    }

    @Test
    public void shouldReturnSelfOnOrElseSupplierIfValid() {
        Validation<List<String>, String> validValidation = valid();
        assertThat(validValidation.orElse(this::invalid)).isSameAs(validValidation);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseIfValid() {
        Validation<List<String>, String> validValidation = valid();
        assertThat(invalid().orElse(validValidation)).isSameAs(validValidation);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseSupplierIfValid() {
        Validation<List<String>, String> validValidation = valid();
        assertThat(invalid().orElse(() -> validValidation)).isSameAs(validValidation);
    }

    // -- getOrElseGet

    @Test
    public void shouldReturnValueOnGetOrElseGetIfValid() {
        Validation<Integer, String> validValidation = valid();
        assertThat(validValidation.getOrElseGet(e -> "error" + e)).isEqualTo(OK);
    }

    @Test
    public void shouldReturnCalculationOnGetOrElseGetIfInvalid() {
        Validation<Integer, String> invalidValidation = Validation.invalid(42);
        assertThat(invalidValidation.getOrElseGet(e -> "error" + e)).isEqualTo("error42");
    }

    // -- fold

    @Test
    public void shouldConvertSuccessToU() {
        Validation<List<String>, String> validValidation = valid();
        Integer result = validValidation.fold(List::size, String::length);
        assertThat(result).isEqualTo(2);
    }

    @Test
    public void shouldConvertFailureToU() {
        Validation<List<String>, String> invalidValidation = invalid();
        Integer result = invalidValidation.fold(List::size, String::length);
        assertThat(result).isEqualTo(3);
    }


    // getOrElseThrow(Function)

    @Test
    public void shouldReturnValidWhenGetOrElseThrowWithFunctionOnValid() {
        final Integer actual = Validation.<String, Integer>valid(1).getOrElseThrow(s -> new RuntimeException(s));
        assertThat(actual).isEqualTo(1);
    }

    @Test
    public void shouldThrowWhenGetOrElseThrowWithFunctionOnInvalid() {
        assertThrows(RuntimeException.class, () -> {

            Validation.<String, Integer>invalid("some error").getOrElseThrow(s -> new RuntimeException(s));
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenGetOrElseThrowNullWithFunctionOnValid() {
        assertThrows(NullPointerException.class, () -> {

            Validation.<String, Integer>valid(1).getOrElseThrow((Function<? super String, RuntimeException>) null);
        });
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenGetOrElseThrowNullWithFunctionOnInvalid() {
        assertThrows(NullPointerException.class, () -> {

            Validation.<String, Integer>invalid("some error").getOrElseThrow((Function<? super String, RuntimeException>) null);
        });
    }

    // -- swap

    @Test
    public void shouldSwapSuccessToFailure() {
        assertThat(valid().swap() instanceof Invalid).isTrue();
        assertThat(valid().swap().getError()).isEqualTo(OK);
    }

    @Test
    public void shouldSwapFailureToSuccess() {
        assertThat(invalid().swap() instanceof Valid).isTrue();
        assertThat(invalid().swap().get()).isEqualTo(ERRORS);
    }

    // -- map

    @Test
    public void shouldMapSuccessValue() {
        assertThat(valid().map(s -> s + "!").get()).isEqualTo(OK + "!");
    }

    @Test
    public void shouldMapFailureError() {
        assertThat(invalid().map(s -> 2).getError()).isEqualTo(ERRORS);
    }

    @Test
    public void shouldMapFailureErrorOnGet() {
        assertThrows(RuntimeException.class, () -> {

            assertThat(invalid().map(s -> 2).get()).isEqualTo(ERRORS);
        });
    }

    // -- bimap

    @Test
    public void shouldMapOnlySuccessValue() {
        Validation<List<String>, String> validValidation = valid();
        Validation<Integer, Integer> validMapping = validValidation.bimap(List::size, String::length);
        assertThat(validMapping instanceof Valid).isTrue();
        assertThat(validMapping.get()).isEqualTo(2);
    }

    @Test
    public void shouldMapOnlyFailureValue() {
        Validation<List<String>, String> invalidValidation = invalid();
        Validation<Integer, Integer> invalidMapping = invalidValidation.bimap(List::size, String::length);
        assertThat(invalidMapping instanceof Invalid).isTrue();
        assertThat(invalidMapping.getError()).isEqualTo(3);
    }

    // -- mapError

    @Test
    public void shouldNotMapSuccess() {
        assertThat(valid().mapError(x -> 2).get()).isEqualTo(OK);
    }

    @Test
    public void shouldMapFailure() {
        assertThat(invalid().mapError(x -> 5).getError()).isEqualTo(5);
    }

    // -- forEach

    @Test
    public void shouldProcessFunctionInForEach() {

        { // Valid.forEach
            java.util.List<String> accumulator = new ArrayList<>();
            Validation<String, String> v1 = Validation.valid("valid");
            v1.forEach(accumulator::add);
            assertThat(accumulator.size()).isEqualTo(1);
            assertThat(accumulator.get(0)).isEqualTo("valid");
        }

        { // Invalid.forEach
            java.util.List<String> accumulator = new ArrayList<>();
            Validation<String, String> v2 = Validation.invalid("error");
            v2.forEach(accumulator::add);
            assertThat(accumulator.size()).isEqualTo(0);
        }
    }

    // -- combine and apply

    @Test
    public void shouldBuildUpForSuccessCombine() {
        Validation<String, String> v1 = Validation.valid("John Doe");
        Validation<String, Integer> v2 = Validation.valid(39);
        Validation<String, Option<String>> v3 = Validation.valid(Option.ofNullable("address"));
        Validation<String, Option<String>> v4 = Validation.valid(Option.none());
        Validation<String, String> v5 = Validation.valid("111-111-1111");
        Validation<String, String> v6 = Validation.valid("alt1");
        Validation<String, String> v7 = Validation.valid("alt2");
        Validation<String, String> v8 = Validation.valid("alt3");
        Validation<String, String> v9 = Validation.valid("alt4");

        Validation<List<String>, TestValidation> result = v1.combine(v2).ap(TestValidation::new);

        Validation<List<String>, TestValidation> result2 = v1.combine(v2).combine(v3).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result3 = v1.combine(v2).combine(v4).ap(TestValidation::new);

        Validation<List<String>, TestValidation> result4 = v1.combine(v2).combine(v3).combine(v5).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result5 = v1.combine(v2).combine(v3).combine(v5).combine(v6).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result6 = v1.combine(v2).combine(v3).combine(v5).combine(v6).combine(v7).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result7 = v1.combine(v2).combine(v3).combine(v5).combine(v6).combine(v7).combine(v8).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result8 = v1.combine(v2).combine(v3).combine(v5).combine(v6).combine(v7).combine(v8).combine(v9).ap(TestValidation::new);

        Validation<List<String>, String> result9 = v1.combine(v2).combine(v3).ap((p1, p2, p3) -> p1 + ":" + p2 + ":" + p3.getOrElse("none"));

        assertThat(result.isValid()).isTrue();
        assertThat(result2.isValid()).isTrue();
        assertThat(result3.isValid()).isTrue();
        assertThat(result4.isValid()).isTrue();
        assertThat(result5.isValid()).isTrue();
        assertThat(result6.isValid()).isTrue();
        assertThat(result7.isValid()).isTrue();
        assertThat(result8.isValid()).isTrue();
        assertThat(result9.isValid()).isTrue();

        assertThat(result.get() instanceof TestValidation).isTrue();
        assertThat(result9.get() instanceof String).isTrue();
    }

    @Test
    public void shouldBuildUpForSuccessMapN() {
        Validation<String, String> v1 = Validation.valid("John Doe");
        Validation<String, Integer> v2 = Validation.valid(39);
        Validation<String, Option<String>> v3 = Validation.valid(Option.ofNullable("address"));
        Validation<String, Option<String>> v4 = Validation.valid(Option.none());
        Validation<String, String> v5 = Validation.valid("111-111-1111");
        Validation<String, String> v6 = Validation.valid("alt1");
        Validation<String, String> v7 = Validation.valid("alt2");
        Validation<String, String> v8 = Validation.valid("alt3");
        Validation<String, String> v9 = Validation.valid("alt4");

        // Alternative map(n) functions to the 'combine' function
        Validation<List<String>, TestValidation> result = Validation.combine(v1, v2).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result2 = Validation.combine(v1, v2, v3).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result3 = Validation.combine(v1, v2, v4).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result4 = Validation.combine(v1, v2, v3, v5).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result5 = Validation.combine(v1, v2, v3, v5, v6).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result6 = Validation.combine(v1, v2, v3, v5, v6, v7).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result7 = Validation.combine(v1, v2, v3, v5, v6, v7, v8).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result8 = Validation.combine(v1, v2, v3, v5, v6, v7, v8, v9).ap(TestValidation::new);

        Validation<List<String>, String> result9 = Validation.combine(v1, v2, v3).ap((p1, p2, p3) -> p1 + ":" + p2 + ":" + p3.getOrElse("none"));

        assertThat(result.isValid()).isTrue();
        assertThat(result2.isValid()).isTrue();
        assertThat(result3.isValid()).isTrue();
        assertThat(result4.isValid()).isTrue();
        assertThat(result5.isValid()).isTrue();
        assertThat(result6.isValid()).isTrue();
        assertThat(result7.isValid()).isTrue();
        assertThat(result8.isValid()).isTrue();
        assertThat(result9.isValid()).isTrue();

        assertThat(result.get() instanceof TestValidation).isTrue();
        assertThat(result9.get() instanceof String).isTrue();
    }

    @Test
    public void shouldBuildUpForFailure() {
        Validation<String, String> v1 = Validation.valid("John Doe");
        Validation<String, Integer> v2 = Validation.valid(39);
        Validation<String, Option<String>> v3 = Validation.valid(Option.ofNullable("address"));

        Validation<String, String> e1 = Validation.invalid("error2");
        Validation<String, Integer> e2 = Validation.invalid("error1");
        Validation<String, Option<String>> e3 = Validation.invalid("error3");

        Validation<List<String>, TestValidation> result = v1.combine(e2).combine(v3).ap(TestValidation::new);
        Validation<List<String>, TestValidation> result2 = e1.combine(v2).combine(e3).ap(TestValidation::new);

        assertThat(result.isInvalid()).isTrue();
        assertThat(result2.isInvalid()).isTrue();
    }

    // -- miscellaneous

    @Test
    public void shouldThrowErrorOnGetErrorValid() {
        assertThrows(RuntimeException.class, () -> {
            Validation<String, String> v1 = valid();
            v1.getError();
        });
    }

    @Test
    public void shouldMatchLikeObjects() {
        Validation<String, String> v1 = Validation.valid("test");
        Validation<String, String> v2 = Validation.valid("test");
        Validation<String, String> v3 = Validation.valid("test diff");

        Validation<String, String> e1 = Validation.invalid("error1");
        Validation<String, String> e2 = Validation.invalid("error1");
        Validation<String, String> e3 = Validation.invalid("error diff");

        assertThat(v1.equals(v1)).isTrue();
        assertThat(v1.equals(v2)).isTrue();
        assertThat(v1.equals(v3)).isFalse();

        assertThat(e1.equals(e1)).isTrue();
        assertThat(e1.equals(e2)).isTrue();
        assertThat(e1.equals(e3)).isFalse();
    }

    @Test
    public void shouldReturnCorrectStringForToString() {
        Validation<String, String> v1 = Validation.valid("test");
        Validation<String, String> v2 = Validation.invalid("error");

        assertThat(v1.toString()).isEqualTo("Valid[value=test]");
        assertThat(v2.toString()).isEqualTo("Invalid[error=error]");
    }

    @Test
    public void shouldReturnHashCode() {
        Validation<String, String> v1 = Validation.valid("test");
        Validation<String, String> e1 = Validation.invalid("error");

        assertThat(v1.hashCode()).isEqualTo(Objects.hashCode(v1));
        assertThat(e1.hashCode()).isEqualTo(Objects.hashCode(e1));
    }

    // ------------------------------------------------------------------------------------------ //

    private <E> Validation<E, String> valid() {
        return Validation.valid(OK);
    }

    private <T> Validation<List<String>, T> invalid() {
        return Validation.invalid(ERRORS);
    }

    static class TestValidation {
        String name;
        Integer age;
        Option<String> address;
        String phone;
        String alt1;
        String alt2;
        String alt3;
        String alt4;

        TestValidation(String name, Integer age) {
            this.name = name;
            this.age = age;
            address = Option.none();
        }

        TestValidation(String name, Integer age, Option<String> address) {
            this.name = name;
            this.age = age;
            this.address = address;
        }

        TestValidation(String name, Integer age, Option<String> address, String phone) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.phone = phone;
        }

        TestValidation(String name, Integer age, Option<String> address, String phone, String alt1) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.phone = phone;
            this.alt1 = alt1;
        }

        TestValidation(String name, Integer age, Option<String> address, String phone, String alt1, String alt2) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.phone = phone;
            this.alt1 = alt1;
            this.alt2 = alt2;
        }

        TestValidation(String name, Integer age, Option<String> address, String phone, String alt1, String alt2, String alt3) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.phone = phone;
            this.alt1 = alt1;
            this.alt2 = alt2;
            this.alt3 = alt3;
        }

        TestValidation(String name, Integer age, Option<String> address, String phone, String alt1, String alt2, String alt3, String alt4) {
            this.name = name;
            this.age = age;
            this.address = address;
            this.phone = phone;
            this.alt1 = alt1;
            this.alt2 = alt2;
            this.alt3 = alt3;
            this.alt4 = alt4;
        }

        @Override
        public String toString() {
            return "TestValidation(" + name + "," + age + "," + address.getOrElse("none") + phone + "," + ")";
        }
    }

    // -- Complete Validation example, may be moved to Vavr documentation later

    @Test
    public void shouldValidateValidPerson() {
        final String name = "John Doe";
        final int age = 30;
        final Validation<List<String>, Person> actual = new PersonValidator().validatePerson(name, age);
        final Validation<List<String>, Person> expected = Validation.valid(new Person(name, age));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldValidateInvalidPerson() {
        final String name = "John? Doe!4";
        final int age = -1;
        final Validation<List<String>, Person> actual = new PersonValidator().validatePerson(name, age);
        final Validation<List<String>, Person> expected = Validation.invalid(List.of(
                "Name contains invalid characters: '!4?'",
                "Age must be greater than 0"
        ));
        assertThat(actual).isEqualTo(expected);
    }

    static class PersonValidator {

        // private final String validNameChars = "[a-zA-Z ]";
        private final int minAge = 0;

        Validation<List<String>, Person> validatePerson(String name, int age) {
            return Validation.combine(validateName(name), validateAge(age)).ap(Person::new);
        }

        private Validation<String, String> validateName(String name) {
            var res = name.chars().mapToObj(c -> (char) c)
                    .filter(c -> !Character.isLetter(c) && c != ' ')
                    .toList();
            return res.isEmpty() ? Validation.<String, String>valid(name)
                    : Validation.<String, String>invalid("Name contains invalid characters: '"
                    + res.stream().distinct().sorted().map(String::valueOf).collect(Collectors.joining()) + "'");
        }

        private Validation<String, Integer> validateAge(int age) {
            return (age < minAge) ? Validation.invalid("Age must be greater than 0")
                    : Validation.valid(age);
        }
    }

    static class Person {

        final String name;
        final int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof Person) {
                final Person person = (Person) o;
                return Objects.equals(name, person.name) && age == person.age;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return "Person(" + name + ", " + age + ")";
        }
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

    // -- transform

    @Test
    public void shouldThrowExceptionOnNullTransformFunction() {
        assertThrows(NullPointerException.class, () -> {

            Validation.valid(1).transform(null);
        });
    }

    @Test
    public void shouldApplyTransformFunctionToRight() {
        final Validation<?, Integer> validation = Validation.valid(1);
        final Function<Validation<?, Integer>, String> f = v -> v.get().toString().concat("-transformed");
        assertThat(validation.transform(f)).isEqualTo("1-transformed");
    }

    @Test
    public void shouldHandleTransformOnLeft() {
        assertThat(Validation.invalid(0).<String>transform(self -> self.isEmpty() ? "ok" : "failed")).isEqualTo("ok");
    }
}

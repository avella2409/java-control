# Java Control

Reuse 90% of the VAVR code/tests **related to controls** and adapt them to work with the Java 17 **sealed** keyword.

### Controls

Each control is a record that implements a sealed interface, therefore they can be used with pattern matching.

A fluent API is available for each control, allowing you to clearly describe what you want.

- Option\<T>
  - Some<T>(T value)
  - None<T>()
- Either<L, R>
  - Right(R value)
  - Left(L value)
- Validation<E, T>
  - Valid(T value)
  - Invalid(E error)
- Try\<T>
  - Success(T value)
  - Failure(Throwable cause)

### Simple Examples

Each control has many more operations; the following is a brief example of how to use it.

##### Option
```java
void optionExamples() {
  // Exhaustivity check (Java 17), Record pattern matching (Java 21)
  switch (Option.some("someValue")) {
    case Some(var value) -> System.out.println(value);
    case None() -> System.out.println("None");
  }

  Option.some("someValue"); // Some("someValue")

  Option.none(); // None()

  Option.ofNullable(null); // None()

  Option.some(45).map(String::valueOf); // Some("45")

  Option.some("value").flatMap(value -> Option.none()); // None()

  Option.some("value").stream(); // Java stream

  Option.some("value").isPresent(); // true

  Option.none().isEmpty(); // true
}
```

##### Either

```java
void eitherExamples() {
  // Exhaustivity check (Java 17), Record pattern matching (Java 21)
  switch (Either.right("value")) {
    case Right(var right) -> System.out.println(right);
    case Left(var left) -> System.out.println(left);
  }

  Either.right("rightValue"); // Right("rightValue")

  Either.left("leftValue"); // Left("leftValue")

  Either.ofNullable(null, "leftValue"); // Left("leftValue")

  Either.right(45).map(String::valueOf); // Right("45")

  Either.right("rightValue").flatMap(value -> Either.left("leftValue")); // Left("leftValue")

  Either.right("rightValue").stream(); // Java stream

  Either.right("rightValue").isRight(); // true

  Either.left("leftValue").isLeft(); // true
}
```

##### Validation

```java
void validationExamples() {
  // Exhaustivity check (Java 17), Record pattern matching (Java 21)
  switch (Validation.valid("valid")) {
    case Valid(var value) -> System.out.println(value);
    case Invalid(var error) -> System.out.println(error);
  }

  Validation.valid("valid"); // Valid("valid")

  Validation.invalid("invalid"); // Invalid("invalid")

  Validation.ofNullable(null, "invalid"); // Invalid("invalid")

  Validation.valid(45).map(String::valueOf); // Valid("45")

  Validation.valid("valid").flatMap(value -> Validation.invalid("invalid")); // Invalid("invalid")

  Validation.valid("valid").stream(); // Java stream

  Validation.valid("valid").isValid(); // true

  Validation.invalid("invalid").isInvalid(); // true

  Validation.combine(
          Validation.valid("s1"), Validation.valid("s2"), Validation.valid("s3")
  ).ap((s1, s2, s3) -> s1 + s2 + s3); // "s1s2s3"
}
```

##### Try

```java
void tryExamples() {
  // Exhaustivity check (Java 17), Record pattern matching (Java 21)
  switch (Try.success("success")) {
    case Success(var value) -> System.out.println(value);
    case Failure(var exception) -> System.out.println(exception);
  }

  Try.success("success"); // Success("success")

  Try.failure(new RuntimeException("Exception")); // Failure(RuntimeException("Exception"))

  Try.ofNullable(null, new RuntimeException("Exception")); // Failure(RuntimeException("Exception"))

  Try.success(45).map(String::valueOf); // Success("45")

  Try.success("success").flatMap(value -> Try.failure(new RuntimeException("Exception"))); // Failure(RuntimeException("Exception"))

  Try.success("success").stream(); // Java stream

  Try.success("success").isSuccess(); // true

  Try.failure(new RuntimeException("Exception")).isFailure(); // true

  Try.of(() -> {
    throw new IOException("checked exception");
  }); // Failure(IOException("checked exception"))

  // Auto close
  Try.withResources(
          () -> new ByteArrayInputStream(new byte[45]),
          () -> new ByteArrayInputStream(new byte[45]),
          () -> new ByteArrayInputStream(new byte[45])
  ).of((input1, input2, input3) ->
          new String(input1.readAllBytes())
                  + new String(input2.readAllBytes())
                  + new String(input3.readAllBytes())
  );
}
```
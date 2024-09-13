package control;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class DirectStyleTest {

    @Test
    public void directStyleOption() {
        assertEquals(
                Option.some("v1v2v3"),
                Option.direct($ -> {
                    var v1 = Option.some("v1").value($);
                    var v2 = Option.some("v2").value($);
                    var v3 = Option.some("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnOption() {
        var processedV3 = new AtomicBoolean(false);
        assertEquals(
                Option.none(),
                Option.direct($ -> {
                    var v1 = Option.some("v1").value($);
                    var v2 = Option.none().value($);
                    processedV3.set(true);
                    var v3 = Option.some("v3").value($);
                    return v1 + v2 + v3;
                })
        );
        assertFalse(processedV3.get());
    }

    @Test
    public void directStyleEither() {
        assertEquals(
                Either.right("v1v2v3"),
                Either.direct($ -> {
                    var v1 = Either.right("v1").value($);
                    var v2 = Either.right("v2").value($);
                    var v3 = Either.right("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnLeft() {
        var processedV3 = new AtomicBoolean(false);
        assertEquals(
                Either.left("Short Circuit"),
                // Most of the time specifying the type of the EitherExtractor will not be necessary
                // it will be directly inferred from the function return type
                Either.direct((EitherExtractor<String> $) -> {
                    var v1 = Either.<String, String>right("v1").value($);
                    var v2 = Either.<String, String>left("Short Circuit").value($);
                    processedV3.set(true);
                    var v3 = Either.<String, String>right("v3").value($);

                    return v1 + v2 + v3;
                })
        );
        assertFalse(processedV3.get());
    }

    @Test
    public void combineEitherAndOption() {
        assertEquals(
                Either.right(Option.some("v1v2v3")),
                Either.direct(eth ->
                        Option.direct(opt -> {
                            var v1 = Either.right("v1").value(eth);
                            var v2 = Option.some("v2").value(opt);
                            var v3 = Either.right("v3").value(eth);
                            return v1 + v2 + v3;
                        }))
        );
    }

    @Test
    public void combineEitherAndOptionWithOptionFailure() {
        assertEquals(
                Either.right(Option.none()),
                Either.direct(eth ->
                        Option.direct(opt -> {
                            var v1 = Either.right("v1").value(eth);
                            var v2 = Option.none().value(opt);
                            var v3 = Either.right("v3").value(eth);
                            return v1 + v2 + v3;
                        }))
        );
    }

    @Test
    public void combineEitherAndOptionWithEitherFailure() {
        assertEquals(
                Either.left("Error"),
                Either.direct((EitherExtractor<String> eth) ->
                        Option.direct(opt -> {
                            var v1 = Either.<String, String>right("v1").value(eth);
                            var v2 = Option.some("v2").value(opt);
                            var v3 = Either.<String, String>left("Error").value(eth);
                            return v1 + v2 + v3;
                        }))
        );
    }

    @Test
    public void directStyleTry() {
        assertEquals(
                Try.success("v1v2v3"),
                Try.direct($ -> {
                    var v1 = Try.success("v1").value($);
                    var v2 = Try.success("v2").value($);
                    var v3 = Try.success("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnFailure() {
        var processedV3 = new AtomicBoolean(false);
        var t = Try.direct($ -> {
            var v1 = Try.success("v1").value($);
            var v2 = Try.failure(new RuntimeException("Short Circuit")).value($);
            processedV3.set(true);
            var v3 = Try.success("v3").value($);
            return v1 + v2 + v3;
        });

        assertTrue(t.isFailure());
        assertEquals("Short Circuit", t.getCause().getMessage());
        assertFalse(processedV3.get());
    }

    @Test
    public void directStyleValidation() {
        assertEquals(
                Validation.valid("v1v2v3"),
                Validation.direct($ -> {
                    var v1 = Validation.valid("v1").value($);
                    var v2 = Validation.valid("v2").value($);
                    var v3 = Validation.valid("v3").value($);
                    return v1 + v2 + v3;
                })
        );
    }

    @Test
    public void shortCircuitOnInvalid() {
        var processedV3 = new AtomicBoolean(false);
        assertEquals(
                Validation.invalid("Short Circuit"),
                Validation.direct((ValidationExtractor<String> $) -> {
                    var v1 = Validation.<String, String>valid("v1").value($);
                    var v2 = Validation.<String, String>invalid("Short Circuit").value($);
                    processedV3.set(true);
                    var v3 = Validation.<String, String>valid("v3").value($);

                    return v1 + v2 + v3;
                })
        );
        assertFalse(processedV3.get());
    }
}

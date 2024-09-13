package control;

import java.util.function.Function;

/**
 * Enable direct style syntax for monads
 * See "Martin Odersky DIRECT STYLE SCALA": https://www.youtube.com/watch?v=0Fm0y4K4YO8
 */
public class Boundary {

    public static <T> void breakNow(T value, Label label) {
        throw new Break(value, label);
    }

    @SuppressWarnings("unchecked")
    public static <T> T apply(Function<Label, T> body) {
        var label = new Label();
        try {
            return body.apply(label);
        } catch (Break b) {
            if (b.label == label) return (T) b.value;
            else throw b;
        }
    }
}

// Fast exception
final class Break extends RuntimeException {

    private static final long serialVersionUID = 1L;

    final Object value;
    final Label label;

    Break(Object value, Label label) {
        super(null, null, false, false);
        this.value = value;
        this.label = label;
    }
}


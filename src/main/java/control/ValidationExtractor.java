package control;

public interface ValidationExtractor<E> {
    <T> T value(Validation<E, T> validation);
}

package control;

public interface EitherExtractor<L> {
    <R> R value(Either<L, R> either);
}

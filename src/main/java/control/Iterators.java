package control;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class Iterators {

    private static final EmptyIterator<Object> empty = new EmptyIterator<>();

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> empty() {
        return (Iterator<T>) empty;
    }

    public static <T> Iterator<T> of(T element) {
        return new SingletonIterator<>(element);
    }

    final static class SingletonIterator<T> implements Iterator<T> {

        private final T element;
        private boolean hasNext = true;

        SingletonIterator(T element) {
            this.element = element;
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            hasNext = false;
            return element;
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (hasNext) {
                action.accept(element);
                hasNext = false;
            }
        }

        @Override
        public String toString() {
            return "SingletonIterator";
        }
    }

    final static class EmptyIterator<T> implements Iterator<T> {

        private EmptyIterator() {
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public T next() {
            throw new NoSuchElementException();
        }

        @Override
        public void forEachRemaining(Consumer<? super T> action) {
            Objects.requireNonNull(action);
        }

        @Override
        public String toString() {
            return "EmptyIterator";
        }
    }
}

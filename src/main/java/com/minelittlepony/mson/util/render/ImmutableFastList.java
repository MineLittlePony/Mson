package com.minelittlepony.mson.util.render;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Consumer;

/**
 * An immutable list implementation based around an array.
 *
 */
@SuppressWarnings("unchecked")
abstract class ImmutableFastList<T> extends AbstractList<T> implements RandomAccess {
    protected ImmutableFastList() { }

    protected abstract Object[] values();

    @Override
    public int indexOf(Object o) {
        Object[] values = values();
        for (int i = 0; i < values.length; i++) {
            Object b = values[i];
            if (b == o || (b != null && b.equals(o))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        Object[] values = values();
        for (int i = values.length - 1; i >= 0; i--) {
            Object b = values[i];
            if (b == o || (b != null && b.equals(o))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return (T)values()[index];
    }

    @Override
    public int size() {
        return values().length;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        Object[] values = values();
        for (int i = 0; i < values.length; i++) {
            action.accept((T)values[i]);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter();
    }

    private final class Iter implements Iterator<T> {
        private int index;

        @Override
        public boolean hasNext() {
            return index < values().length;
        }

        @Override
        public T next() {
            Object[] values = values();
            if (++index < values().length) {
                return (T)values[index];
            }
            throw new NoSuchElementException();
        }
    }
}

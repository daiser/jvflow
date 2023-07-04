package com.github.daiser.flow;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class ArraysAreFuckingIterable<T> implements Iterable<T>, Iterator<T> {
    private final T[] array;
    private final int length;
    private int currentIndex = -1;

    public ArraysAreFuckingIterable(T[] array) {
        this.array = array;
        this.length = array.length;
    }

    public static <T> Iterable<T> fixJava(T[] array) {
        return new ArraysAreFuckingIterable<>(array);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return this.currentIndex + 1 < this.length;
    }

    @Override
    public T next() {
        return this.array[++this.currentIndex];
    }
}

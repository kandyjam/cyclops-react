package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class FillSpliterator<T> implements Spliterator<T> {

    private final T value;
    
 

    @Override
    public long estimateSize() {
        return 1l;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }


    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
       
        action.accept(value);
        

    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

    
}
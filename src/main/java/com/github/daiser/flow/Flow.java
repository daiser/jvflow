package com.github.daiser.flow;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class Flow<I, O> implements Input<I> {
    private final List<Input<O>> outputs = new ArrayList<>();
    private final Function<I, O> processor;

    public Flow(Function<I, O> processor) {
        this.processor = processor;
    }

    public static <V> Flow<V, V> start() {
        return new Flow<>((V value) -> value);
    }

    protected void attach(Input<O> output) {
        this.outputs.add(output);
    }

    @Override
    public void accept(I value) {
        var result = this.processor.apply(value);
        if (result != null) {
            for (var output : outputs) {
                output.accept(result);
            }
        }
    }

    public void acceptMany(Iterable<I> values) {
        for (var value : values) {
            this.accept(value);
        }
    }

    public void collectTo(List<O> to) {
        this.attach(new Flow<>(to::add));
    }

    public ArrayList<O> collect() {
        var values = new ArrayList<O>();
        this.collectTo(values);
        return values;
    }

    public Flow<O, O> filter(Predicate<O> filter) {
        var filterFlow = new Flow<>((O value) -> filter.test(value) ? value : null);
        this.attach(filterFlow);
        return filterFlow;
    }

    public <V> Flow<O, V> map(Function<O, V> mapper) {
        var mappedFlow = new Flow<>(mapper);
        this.attach(mappedFlow);
        return mappedFlow;
    }

    public Flow<O, O> peep(Consumer<O> observer) {
        var observedFlow = new Flow<>((O value) -> {
            observer.accept(value);
            return value;
        });
        this.attach(observedFlow);
        return observedFlow;
    }

    @SafeVarargs
    public final <C> ArrayList<Flow<O, O>> segregate(Function<O, Iterable<C>> classify, C... classes) {
        return this.segregate(classify, ArraysAreFuckingIterable.fixJava(classes));
    }

    public final <C> ArrayList<Flow<O, O>> segregate(Function<O, Iterable<C>> classify, Iterable<C> classes) {
        var classificator = new Classificator<>(classify, classes);
        this.attach(classificator);
        return classificator.getFlows();
    }
}


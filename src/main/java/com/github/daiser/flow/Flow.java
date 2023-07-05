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

    public <S> Flow<S, S> select(Function<O, Iterable<S>> selector) {
        var s = new Selector<>(selector);
        this.attach(s);
        return s.outFlow;
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

    private static final class Classificator<V, C> implements Input<V> {
        private final Function<V, Iterable<C>> classify;
        private final Map<C, Flow<V, V>> flowMap = new HashMap<>();
        private final Flow<V, V> unclassified;
        private final ArrayList<Flow<V, V>> flows = new ArrayList<>();

        public Classificator(Function<V, Iterable<C>> classify, Iterable<C> classes, boolean withUnclassified) {
            this.classify = classify;
            for (var c : classes) {
                Flow<V, V> classFlow = Flow.start();
                this.flows.add(classFlow);
                this.flowMap.put(c, classFlow);
            }

            this.unclassified = withUnclassified ? Flow.start() : null;
            if (this.unclassified != null) {
                this.flows.add(this.unclassified);
            }
        }

        public Classificator(Function<V, Iterable<C>> classify, Iterable<C> classes) {
            this(classify, classes, false);
        }


        public ArrayList<Flow<V, V>> getFlows() {
            return this.flows;
        }

        @Override
        public void accept(V value) {
            var classes = this.classify.apply(value);
            for (var class_ : classes) {
                var flow = this.flowMap.getOrDefault(class_, this.unclassified);
                if (flow != null) {
                    flow.accept(value);
                }
            }
        }
    }

    private static final class Selector<I, O> implements Input<I> {
        public final Flow<O, O> outFlow = Flow.start();
        private final Function<I, Iterable<O>> selector;

        public Selector(Function<I, Iterable<O>> selector) {
            this.selector = selector;
        }


        @Override
        public void accept(I value) {
            this.outFlow.acceptMany(this.selector.apply(value));
        }
    }

}


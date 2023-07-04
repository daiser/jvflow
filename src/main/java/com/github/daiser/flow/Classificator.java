package com.github.daiser.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Classificator<V, C> implements Input<V> {
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

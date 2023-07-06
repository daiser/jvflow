package com.github.daiser.flow;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class FlowTest {
    @Test
    void acceptAndPeep() {
        class LastValue<T> {
            public T value;
        }

        // cooking
        final var seen = new LastValue<>();
        var f = Flow.<Integer>start();
        f.peep((v) -> seen.value = v);

        // running
        f.accept(1);

        // checking
        assertEquals(seen.value, 1);
    }

    @Test
    void acceptMany() {
        // cooking
        var f = Flow.<Integer>start();
        var passed = f.collect();

        // running
        f.acceptMany(new Range(1, 10));

        // checking
        assertArrayEquals(
                new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9,},
                passed.toArray(new Integer[0])
        );
    }


    @Test
    void collectTo() {
        // cooking
        var integers = new ArrayList<Integer>();
        var f = Flow.<Integer>start();
        f.collectTo(integers);

        // running
        f.accept(1);
        f.accept(2);

        // checking
        assertArrayEquals(
                new Integer[]{1, 2,},
                integers.toArray(new Integer[0])
        );
    }

    @Test
    void collect() {
        // cooking
        var f = Flow.<Integer>start();
        var integers = f.collect();

        // running
        f.acceptMany(new Range(1, 5));

        // checking
        assertArrayEquals(
                new Integer[]{1, 2, 3, 4},
                integers.toArray(new Integer[0])
        );
    }

    @Test
    void filter() {
        // cooking
        var f = Flow.<Integer>start();
        var lt5 = f.filter(v -> v < 5).collect();

        // running
        f.acceptMany(new Range(1, 10));

        // checking
        assertArrayEquals(
                new Integer[]{1, 2, 3, 4},
                lt5.toArray(new Integer[0])
        );
    }

    @Test
    void map() {
        // cooking
        var f = Flow.<Integer>start();
        var strings = f.map((v) -> String.format("%d", v)).collect();

        // running
        f.acceptMany(new Range(1, 6));

        // checking
        assertArrayEquals(
                new String[]{"1", "2", "3", "4", "5",},
                strings.toArray(new String[0])
        );
    }

    @Test
    void select() {
        // cooking
        var filesystem = new HashMap<String, List<String>>() {{
            put("folder1", Arrays.asList("file1", "file2"));
            put("folder2", Arrays.asList("file3", "file4"));
        }};
        var f = Flow.<String>start();
        var files = f.select(filesystem::get).collect();

        // running
        f.accept("folder1");
        f.accept("folder2");

        // checking
        assertArrayEquals(
                new String[]{"file1", "file2", "file3", "file4"},
                files.toArray(new String[0])
        );
    }

    @Test
    void segregateNoUnclassified() {
        // cooking
        var f = Flow.<Integer>start();
        Function<Integer, Iterable<String>> classify =
                (Integer n) -> ArraysAreFuckingIterable.fixJava(
                        (n & 1) == 0 ?
                                new String[]{"even"} :
                                new String[]{"odd"}
                );
        var flows = f.segregate(classify, "even", "odd");
        var evens = flows.get(0).collect();
        var odds = flows.get(1).collect();

        // running
        f.acceptMany(new Range(1, 10));

        // checking
        assertArrayEquals(
                new Integer[]{1, 3, 5, 7, 9},
                odds.toArray(new Integer[0])
        );
        assertArrayEquals(
                new Integer[]{2, 4, 6, 8,},
                evens.toArray(new Integer[0])
        );
    }

    @Test
    void segregateUnclassified() {
        // cooking
        var f = Flow.<Integer>start();
        Function<Integer, Iterable<String>> classify = (Integer n) -> {
            var classes = new ArrayList<String>();
            for (var div = 2; div < 10; div++) {
                if (n % div == 0) {
                    classes.add(String.format("div%d", div));
                }
            }

            return classes;
        };
        var flows = f.segregateWithUnclassified(classify, "div5");
        var div5s = flows.get(0).collect();
        var others = flows.get(1).collect();

        // running
        f.acceptMany(new Range(1, 51));

        // checking
        assertArrayEquals(
                new Integer[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50},
                div5s.toArray(new Integer[0])
        );
        assertEquals(40, others.size());
    }

    static class Range implements Iterable<Integer>, Iterator<Integer> {
        private final int to;
        private final int step;
        private int current;

        public Range(int from, int to, int step) {
            this.to = to;
            this.step = step;
            this.current = from;
        }

        public Range(int from, int to) {
            this(from, to, 1);
        }

        @NotNull
        @Override
        public Iterator<Integer> iterator() {
            return this;
        }

        @Override
        public boolean hasNext() {
            return this.current + this.step <= this.to;
        }

        @Override
        public Integer next() {
            try {
                return this.current;
            } finally {
                this.current += this.step;
            }
        }
    }
}
package com.github.daiser;

import com.github.daiser.flow.Flow;

import java.util.HashSet;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        Flow<Integer, Integer> f = Flow.start();

        var flows = f.segregate(Main::classify, "even", "odd");
        Flow<Integer, Integer> evens = flows.get(0), odds = flows.get(1);

        var allEvens = evens.peep(v -> System.out.printf("%d is even\n", v)).collect();
        odds.peep(v -> System.out.printf("%d is odd\n", v));

        for (var i = 0; i < 100; i++) {
            f.accept(i);
        }

        System.out.println(allEvens);
    }

    private static HashSet<String> classify(Integer n) {
        var classes = new HashSet<String>();
        classes.add((n & 1) == 0 ? "even" : "odd");

        for (var i = 2; i < 10; i++) {
            if (n % i == 0) {
                classes.add(String.format("div%d", i));
            }
            if (i == 2) {
                classes.add((n % 2 == 0) ? "even" : "odd");
            }
        }

        return classes;
    }
}
package com.bennyhuo.java17;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

public class RandomGenerators {

    public static void main(String[] args) {
//        randomWithLegacy();
//        randomWithLegacyThreadLocal();
        randomWithNewImplementations();
    }

    public static void randomWithNewImplementations() {
        var random = RandomGenerator.of("L32X64MixRandom");
        for (int i = 0; i < 10; i++) {
            System.out.println(random.nextInt());
        }
    }

    public static void randomWithLegacy() {
        var random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            System.out.println(random.nextInt());
        }
    }

    public static void randomWithLegacyThreadLocal() {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                for (int j = 0; j < 10; j++) {
                    System.out.println(Thread.currentThread().getName() + ": " + ThreadLocalRandom.current().nextInt());
                }
            }, "Thread-" + i).start();
        }
    }

}

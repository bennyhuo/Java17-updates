package com.bennyhuo.java17;

import java.util.function.Consumer;

public sealed interface List<T> {
    public static final class Cons<T> implements List<T> {
        public final T head;

        public final List<T> tail;

        public Cons(T head, List<T> tail) {
            this.head = head;
            this.tail = tail;
        }

    }

    public final class Nil implements List {
        public static final Nil INSTANCE = new Nil();

        private Nil() {
        }
    }

    default void forEach(Consumer<? super T> action) {
        switch (this) {
            case Cons<T> cons -> {
                action.accept(cons.head);
                cons.tail.forEach(action);
            }
            case Nil nil -> {}
        }
    }

    public static <T> List<T> fromArray(T[] array) {
        if (array.length == 0) return Nil.INSTANCE;
        var result = new Cons<T>(array[array.length - 1], Nil.INSTANCE);
        for (int i = array.length - 2; i >= 0; i--) {
            result = new Cons<T>(array[i], result);
        }
        return result;
    }

    public static void main(String[] args) {
        var list = List.fromArray(new Integer[]{1, 2, 3, 4, 5});
        list.forEach(i -> System.out.println(i));
    }
}
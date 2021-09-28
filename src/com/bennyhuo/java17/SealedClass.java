package com.bennyhuo.java17;

/**
 * Created by benny.
 */
public class SealedClass {



    public static void main(String[] args) {
        Root r = new Root.A();
        var x = switch (r) {
            case Root.A a -> 1;
            case Root.B b -> 2;
            case Root.C c -> 3;
        };
        System.out.println(x);
    }

}

abstract sealed class Root {
    static final class A extends Root { }

    static sealed class B extends Root {
        static final class B1 extends B {}
        static final class B2 extends B {}
    }

    static non-sealed class C extends Root { }
}
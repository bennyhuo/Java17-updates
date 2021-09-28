package com.bennyhuo.java17;

/**
 * Created by benny.
 */
public class PatternMatchForSwitch {
    public static void main(String[] args) {
        System.out.println(formatterPatternSwitch("hello"));
    }

    static String formatterPatternSwitch(Object o) {
        return switch (o) {
            case null -> "null";
            case Integer i -> String.format("int %d", i);
            case Long l -> String.format("long %d", l);
            case Double d -> String.format("double %f", d);
            case String s -> String.format("String %s", s);
            default -> o.toString();
        };
    }
}

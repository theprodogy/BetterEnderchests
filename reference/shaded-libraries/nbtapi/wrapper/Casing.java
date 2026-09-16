/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.wrapper;

import java.util.function.UnaryOperator;

public enum Casing {
    camelCase(s -> {
        if (s.length() < 2) {
            return s.toLowerCase();
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }),
    snake_case(s -> {
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(s.charAt(0)));
        for (int i = 1; i < s.length(); ++i) {
            char currentChar = s.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                result.append('_').append(Character.toLowerCase(currentChar));
                continue;
            }
            result.append(currentChar);
        }
        return result.toString();
    }),
    PascalCase(s -> {
        if (s.length() < 2) {
            return s.toUpperCase();
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }),
    lowercase(String::toLowerCase),
    UPPERCASE(String::toUpperCase);

    private UnaryOperator<String> convert;

    private Casing(UnaryOperator<String> function) {
        this.convert = function;
    }

    public String convertString(String str) {
        return (String)this.convert.apply(str);
    }
}


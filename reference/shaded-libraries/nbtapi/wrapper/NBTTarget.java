/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.nbtapi.wrapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface NBTTarget {
    public String value();

    public Type type() default Type.AUTOMATIC;

    public static enum Type {
        AUTOMATIC,
        GET,
        SET,
        HAS;

    }
}


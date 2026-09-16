/*
 * Decompiled with CFR 0.152.
 */
package com.fernsehheft.enderchest.faststats.core.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NonNls;

@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@NonNls
@Pattern(value="[a-z_]+")
public @interface SourceId {
    public static final String PATTERN = "[a-z_]+";
}


/*
 * Decompiled with CFR 0.152.
 */
package org.jspecify.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(value={ElementType.TYPE_USE})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface NonNull {
}


package com.game.sdk.erating.domain.base;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by lucky on 2018/2/1.
 */
@Documented
@Target({TYPE, FIELD})
@Retention(RUNTIME)
public @interface NodeName {
    String name() default "";

    boolean object() default false;
}

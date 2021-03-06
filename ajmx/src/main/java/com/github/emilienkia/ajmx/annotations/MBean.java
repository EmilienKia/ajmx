package com.github.emilienkia.ajmx.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MBean {

    String domain() default "";

    String type() default "";

    String name() default "";

    String description() default "";

    // TODO extra properties
}

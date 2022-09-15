package me.felixnaumann.fwebserver.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CliCommandName {
    String name();
    String description() default "";
    boolean implemented() default true;
}
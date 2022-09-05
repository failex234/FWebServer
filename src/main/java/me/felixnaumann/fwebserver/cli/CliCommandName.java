package me.felixnaumann.fwebserver.cli;

import jdk.jfr.Enabled;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CliCommandName {
    String value();
    boolean implemented() default true;
}
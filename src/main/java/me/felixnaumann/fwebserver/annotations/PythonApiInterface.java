package me.felixnaumann.fwebserver.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PythonApiInterface {
    boolean instanceNeeded() default false;
}

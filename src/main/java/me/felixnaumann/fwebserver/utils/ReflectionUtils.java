package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.cli.CliCommandName;
import me.felixnaumann.fwebserver.cli.CliCommands;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
    public static Method[] getCliCommandMethods() {
        Method[] allmethods = CliCommands.class.getDeclaredMethods();
        allmethods = Arrays.stream(allmethods).filter(method -> {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == CliCommandName.class) return true;
            }
            return false;
        }).toArray(Method[]::new);

        return allmethods;
    }

    public static Method getCliMethod(String commandName) {
        Method[] cliCommandMethods = ReflectionUtils.getCliCommandMethods();

        Method[] cliCommandMatch = Arrays.stream(cliCommandMethods).filter(method -> method.getAnnotation(CliCommandName.class).value().equals(commandName)).toArray(Method[]::new);
        if (cliCommandMatch.length == 0) return null;
        if (cliCommandMatch.length > 1) {
            System.err.printf("WARNING: multiple methods defined with command name %s. Only running first method.\n", commandName);
        }
        return cliCommandMatch[0];
    }
}

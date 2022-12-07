package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.FWebServer;
import me.felixnaumann.fwebserver.annotations.PythonApiInterface;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

public class ReflectionUtils {
    private static Set<Class<?>> pythonApiClassesDb = new HashSet<>();

    public static Set<Class<?>> getPythonApiClassesDb() {
        return pythonApiClassesDb;
    }

    public static void initializeDb() {
        Reflections reflections = new Reflections(FWebServer.class.getPackageName());
        pythonApiClassesDb =  reflections.getTypesAnnotatedWith(PythonApiInterface.class);
    }
}

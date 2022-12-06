package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.FWebServer;
import me.felixnaumann.fwebserver.annotations.PythonApiInterface;
import org.reflections.Reflections;

import java.util.Set;

public class ReflectionUtils {
    private static Set<Class<?>> pythonApiClassesDb;

    //Builds the database of all classes that can be called from the python api
    static {
        Reflections reflections = new Reflections(FWebServer.class.getPackage().getName());
        pythonApiClassesDb =  reflections.getTypesAnnotatedWith(PythonApiInterface.class);
    }

    public static Set<Class<?>> getPythonApiClassesDb() {
        return pythonApiClassesDb;
    }
}

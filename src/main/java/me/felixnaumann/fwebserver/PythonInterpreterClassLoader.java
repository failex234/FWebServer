package me.felixnaumann.fwebserver;

import me.felixnaumann.fwebserver.exception.PyfsAccessViolation;
import me.felixnaumann.fwebserver.utils.LogUtils;
import me.felixnaumann.fwebserver.utils.ReflectionUtils;

import java.util.Set;
import java.util.stream.Collectors;

public class PythonInterpreterClassLoader extends ClassLoader {

    @Override
    protected synchronized Class<?> loadClass(final String className, final boolean resolve)
            throws ClassNotFoundException
    {

        if (className.startsWith(FWebServer.class.getPackageName()))
        {
            //Already get the class to throw an exception when the class was not found
            //When the class does not exist we don't want to progress any further
            Class<?> clazz = PythonInterpreterClassLoader.getSystemClassLoader().loadClass(className);

            Set<String> allowedClasses = ReflectionUtils.getPythonApiClassesDb().stream().map(Class::getName).collect(Collectors.toSet());

            if (allowedClasses.contains(className)) return clazz;
            throw new PyfsAccessViolation(className);
        }

        return PythonInterpreterClassLoader.getSystemClassLoader().loadClass(className);

    }
}

package me.felixnaumann.fwebserver;

import me.felixnaumann.fwebserver.utils.ReflectionUtils;
import org.python.util.PythonInterpreter;

import java.util.Set;
import java.util.stream.Collectors;


public class PythonInterpreterWrapper extends PythonInterpreter {
    @Override
    public void exec(String s) {
        //Check if the python code tries to access any classes inside the main package
        if (s.contains(FWebServer.class.getPackageName())) {
            Set<String> allowedClassNames = ReflectionUtils.getPythonApiClassesDb().stream().map(Class::getName).collect(Collectors.toSet());
            for (String className : allowedClassNames) {
                if (s.contains(className)) super.exec(s);
                return;
            }
            throw new RuntimeException(String.format("class access in '%s' not allowed.", s));

        }
        super.exec(s);
    }
}

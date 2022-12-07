package me.felixnaumann.fwebserver.exception;

public class PyfsAccessViolation extends RuntimeException {
    public PyfsAccessViolation(String className) {
        super(String.format("Access to class %s is restricted", className));
    }
}

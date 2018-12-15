package com.ifillbrito.idea.immutable;

public class NoConstructorFoundException extends RuntimeException {

    public NoConstructorFoundException() {
        super("The target class does not have a constructor. The class must have at least one constructor.");
    }
}

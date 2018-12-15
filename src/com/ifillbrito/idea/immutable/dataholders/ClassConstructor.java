package com.ifillbrito.idea.immutable.dataholders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ClassConstructor {

    private List<MethodArgument> arguments = new ArrayList<>();

    void addArgument(String type, String name) {
        MethodArgument methodArgument = new MethodArgument();
        methodArgument.setType(type);
        methodArgument.setName(name);
        arguments.add(methodArgument);
    }

    List<MethodArgument> getArguments() {
        return arguments;
    }

    List<String> getMethodArguments() {
        return arguments.stream()
                .map(arg -> String.format("%s %s", arg.getType(), arg.getPlainName()))
                .collect(Collectors.toList());
    }

    List<String> getArgumentValues() {
        return arguments.stream()
                .map(MethodArgument::getPlainName)
                .collect(Collectors.toList());
    }
}

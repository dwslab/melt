package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.kbert;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum InputTypes {
    TARGET("t"),
    SUBJECT("s"),
    OBJECT("o");

    private final String name;
    private static final Map<String, InputTypes> name2InputTypes = Arrays.stream(InputTypes.values())
                    .collect(Collectors.toMap(InputTypes::getName, Function.identity()));

    InputTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static InputTypes fromName(String name) {
        return name2InputTypes.get(name);
    }
}

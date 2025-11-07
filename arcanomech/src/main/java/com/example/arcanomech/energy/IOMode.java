package com.example.arcanomech.energy;

public enum IOMode {
    DISABLED(false, false),
    INPUT(true, false),
    OUTPUT(false, true),
    BOTH(true, true);

    private final boolean allowsInput;
    private final boolean allowsOutput;

    IOMode(boolean allowsInput, boolean allowsOutput) {
        this.allowsInput = allowsInput;
        this.allowsOutput = allowsOutput;
    }

    public boolean allowsInput() {
        return allowsInput;
    }

    public boolean allowsOutput() {
        return allowsOutput;
    }

    public int getId() {
        return ordinal();
    }

    public static IOMode fromId(int id) {
        IOMode[] values = values();
        if (id < 0 || id >= values.length) {
            return DISABLED;
        }
        return values[id];
    }
}

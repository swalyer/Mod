package com.example.arcanomech.debug;

public final class DebugConfig {
    private static boolean enabled;

    private DebugConfig() {
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        DebugConfig.enabled = enabled;
    }
}

package com.example.arcanomech.energy;

import java.util.Arrays;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

public final class SideConfig {
    private static final int SIDES = Direction.values().length;
    private final IOMode[] modes = new IOMode[SIDES];

    private SideConfig(IOMode defaultMode) {
        Arrays.fill(modes, defaultMode);
    }

    public static SideConfig all(IOMode mode) {
        return new SideConfig(mode);
    }

    public IOMode get(Direction direction) {
        return modes[direction.ordinal()];
    }

    public void set(Direction direction, IOMode mode) {
        modes[direction.ordinal()] = mode;
    }

    public void writeNbt(NbtCompound nbt, String key) {
        int[] raw = new int[SIDES];
        for (int i = 0; i < SIDES; i++) {
            raw[i] = modes[i].getId();
        }
        nbt.putIntArray(key, raw);
    }

    public void readNbt(NbtCompound nbt, String key) {
        int[] raw = nbt.getIntArray(key);
        if (raw.length != SIDES) {
            return;
        }
        for (int i = 0; i < SIDES; i++) {
            modes[i] = IOMode.fromId(raw[i]);
        }
    }
}

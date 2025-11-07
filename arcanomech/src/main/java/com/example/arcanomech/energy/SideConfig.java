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

    public IOMode cycle(Direction direction) {
        IOMode next = get(direction).next();
        set(direction, next);
        return next;
    }

    public void setAll(IOMode mode) {
        Arrays.fill(modes, mode);
    }

    public void copyFrom(SideConfig other) {
        if (other == null) {
            return;
        }
        for (Direction direction : Direction.values()) {
            set(direction, other.get(direction));
        }
    }

    public int[] toIdArray() {
        int[] raw = new int[SIDES];
        for (int i = 0; i < SIDES; i++) {
            raw[i] = modes[i].getId();
        }
        return raw;
    }

    public void readFromIds(int[] raw) {
        if (raw.length != SIDES) {
            return;
        }
        for (int i = 0; i < SIDES; i++) {
            modes[i] = IOMode.fromId(raw[i]);
        }
    }

    public void writeNbt(NbtCompound nbt, String key) {
        nbt.putIntArray(key, toIdArray());
    }

    public void readNbt(NbtCompound nbt, String key) {
        readFromIds(nbt.getIntArray(key));
    }
}

package com.example.arcanomech.energy;

import java.util.EnumMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

public final class SideConfig {
    private final EnumMap<Direction, IOMode> modes;

    private SideConfig(EnumMap<Direction, IOMode> modes) {
        this.modes = modes;
    }

    /** Фабрика: все стороны в один режим */
    public static SideConfig all(IOMode mode) {
        EnumMap<Direction, IOMode> map = new EnumMap<>(Direction.class);
        for (Direction d : Direction.values()) {
            map.put(d, mode);
        }
        return new SideConfig(map);
    }

    /** Считать режим стороны */
    public IOMode get(Direction side) {
        return modes.get(side);
    }

    /** Установить режим для стороны */
    public void set(Direction side, IOMode mode) {
        modes.put(side, mode);
    }

    /** Установить режим для всех сторон */
    public void setAll(IOMode mode) {
        for (Direction d : Direction.values()) {
            modes.put(d, mode);
        }
    }

    /** Прокрутить режим по циклу для стороны и вернуть новый */
    public IOMode cycle(Direction side) {
        IOMode cur = modes.getOrDefault(side, IOMode.DISABLED);
        IOMode next = switch (cur) {
            case DISABLED -> IOMode.INPUT;
            case INPUT    -> IOMode.OUTPUT;
            case OUTPUT   -> IOMode.DISABLED;
            default       -> IOMode.DISABLED; // на случай будущих значений/нестандартных ситуаций
        };
        modes.put(side, next);
        return next;
    }


    /** Полная копия значений из другого SideConfig */
    public void copyFrom(SideConfig other) {
        for (Direction d : Direction.values()) {
            modes.put(d, other.modes.get(d));
        }
    }

    /** Сериализация в NBT (как int[6] по порядку Direction.ordinal()) */
    public void writeNbt(NbtCompound nbt, String key) {
        nbt.putIntArray(key, toIdArray());
    }

    /** Десериализация из NBT (int[6]) */
    public void readNbt(NbtCompound nbt, String key) {
        int[] raw = nbt.getIntArray(key);
        if (raw != null && raw.length == 6) {
            readFromIds(raw);
        }
    }

    /** Для хранения в предметах (см. WrenchItem) */
    public int[] toIdArray() {
        int[] arr = new int[6];
        for (Direction d : Direction.values()) {
            arr[d.ordinal()] = modes.get(d).ordinal();
        }
        return arr;
    }

    /** Восстановить из int[6] (ordinal -> enum) */
    public void readFromIds(int[] raw) {
        Direction[] dirs = Direction.values();
        IOMode[] all = IOMode.values();
        for (int i = 0; i < dirs.length && i < raw.length; i++) {
            int id = raw[i];
            IOMode mode = (id >= 0 && id < all.length) ? all[id] : IOMode.DISABLED;
            modes.put(dirs[i], mode);
        }
    }
}

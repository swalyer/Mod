package com.example.arcanomech.energy;

public final class ManaOps {
    private ManaOps() {}

    public static int transfer(ManaStorage from, ManaStorage to, int max) {
        int canExtract = Math.min(max, from.getMana());
        if (canExtract <= 0) return 0;
        int accept = to.insert(canExtract, true);
        if (accept <= 0) return 0;
        int extracted = from.extract(accept, false);
        if (extracted <= 0) return 0;
        to.insert(extracted, false);
        return extracted;
    }

    public static int equalize(ManaStorage a, ManaStorage b, int maxPerTick, int epsilon) {
        int total = a.getMana() + b.getMana();
        int targetA = total / 2;
        int delta = targetA - a.getMana();
        if (Math.abs(delta) <= epsilon) return 0;
        int move = Math.min(Math.abs(delta), maxPerTick);
        if (delta > 0) {
            return transfer(b, a, move);
        } else {
            return transfer(a, b, move);
        }
    }
}

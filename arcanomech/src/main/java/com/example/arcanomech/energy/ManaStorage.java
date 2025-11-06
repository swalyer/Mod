package com.example.arcanomech.energy;

public interface ManaStorage {
    int getMana();

    int getCapacity();

    int insert(int amount, boolean simulate);

    int extract(int amount, boolean simulate);
}

package com.example.arcanomech.energy.net;

public interface ManaNode {
    int getCapacity();

    int getStored();

    int insert(int amount, boolean simulate);

    int extract(int amount, boolean simulate);

    int getMaxIoPerTick();
}

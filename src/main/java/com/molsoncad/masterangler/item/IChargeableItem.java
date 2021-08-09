package com.molsoncad.masterangler.item;

public interface IChargeableItem
{
    float getPowerForTime(float ticks);

    default int getChargeDuration()
    {
        return 72000;
    }
}

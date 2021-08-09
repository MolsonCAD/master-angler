package com.molsoncad.masterangler.capability;

public interface IFishingProperties
{
    boolean isCaught();

    IFishingProperties setCaught(boolean caught);

    boolean isLuring();

    IFishingProperties setLuring(boolean luring);

    float getLuck();

    IFishingProperties setLuck(float luck);
}

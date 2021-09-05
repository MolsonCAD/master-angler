package com.molsoncad.masterangler.capability;

public interface IFishingProperties
{
    boolean isCaught();

    IFishingProperties setCaught(boolean caught);

    boolean isFishing();

    IFishingProperties setFishing(boolean fishing);

    float getLuck();

    IFishingProperties setLuck(float luck);
}

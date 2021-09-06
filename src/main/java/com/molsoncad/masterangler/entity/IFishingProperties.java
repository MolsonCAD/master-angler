package com.molsoncad.masterangler.entity;

public interface IFishingProperties
{
    boolean canBeFished(long daytime);

    boolean isFishing();

    void setFishing(boolean fishing);

    boolean isCaught();

    void setCaught(boolean caught);

    float getLuck();

    void setLuck(float luck);
}

package com.molsoncad.masterangler.item;

public enum FishingRodTier implements IFishingRodTier
{
    WOOD(64, 32.0F, 0.02F),
    BONE(32, 32.0F, 0.02F),
    BAMBOO(96, 32.0F, 0.02F),
    SCUTE(128, 32.0F, 0.02F),
    NETHERITE(192, 32.0F, 0.02F);

    private final int uses;
    private final float strength;
    private final float speed;

    FishingRodTier(int uses, float strength, float speed)
    {
        this.uses = uses;
        this.strength = strength;
        this.speed = speed;
    }

    @Override
    public int getUses()
    {
        return uses;
    }

    @Override
    public float getReelSpeed()
    {
        return speed;
    }

    @Override
    public float getLineStrength()
    {
        return strength;
    }
}

package com.molsoncad.masterangler.capability;

import com.molsoncad.masterangler.MasterAngler;
import net.minecraft.util.ResourceLocation;

public class FishingProperties implements IFishingProperties
{
    public static final ResourceLocation ID = new ResourceLocation(MasterAngler.MODID, "fishing_properties");

    protected boolean caught;
    protected boolean luring;
    protected float luck;

    @Override
    public boolean isCaught()
    {
        return caught;
    }

    @Override
    public IFishingProperties setCaught(boolean caught)
    {
        this.caught = caught;
        return this;
    }

    @Override
    public boolean isLuring()
    {
        return luring;
    }

    @Override
    public IFishingProperties setLuring(boolean luring)
    {
        this.luring = luring;
        return this;
    }

    @Override
    public float getLuck()
    {
        return luck;
    }

    @Override
    public IFishingProperties setLuck(float luck)
    {
        this.luck = luck;
        return this;
    }
}

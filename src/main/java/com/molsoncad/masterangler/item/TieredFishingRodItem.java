package com.molsoncad.masterangler.item;

import net.minecraft.item.FishingRodItem;

public class TieredFishingRodItem extends FishingRodItem implements ITieredFishingRodItem
{
    private final IFishingRodTier tier;

    public TieredFishingRodItem(IFishingRodTier tier, Properties properties)
    {
        super(properties.durability(tier.getUses()));
        this.tier = tier;
    }

    @Override
    public IFishingRodTier getTier()
    {
        return tier;
    }
}

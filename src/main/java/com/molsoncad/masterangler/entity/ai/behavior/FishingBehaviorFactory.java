package com.molsoncad.masterangler.entity.ai.behavior;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Objects;

public abstract class FishingBehaviorFactory extends ForgeRegistryEntry<FishingBehaviorFactory>
{
    public FishingBehaviorFactory(EntityType<?> type)
    {
        setRegistryName(Objects.requireNonNull(type.getRegistryName()));
    }

    public abstract FishingBehavior create(AbstractFishEntity mob);
}

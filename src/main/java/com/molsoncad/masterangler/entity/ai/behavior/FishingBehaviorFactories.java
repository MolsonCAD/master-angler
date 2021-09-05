package com.molsoncad.masterangler.entity.ai.behavior;

import com.molsoncad.masterangler.MasterAngler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Objects;

public class FishingBehaviorFactories
{
    private static IForgeRegistry<FishingBehaviorFactory> REGISTRY;

    public static FishingBehavior createFromInstance(AbstractFishEntity mob)
    {
        return Objects.requireNonNull(REGISTRY.getValue(mob.getType().getRegistryName())).create(mob);
    }

    @SubscribeEvent
    public static void onRegisterFactories(RegistryEvent.Register<FishingBehaviorFactory> event)
    {
        REGISTRY = event.getRegistry();

        // TODO: mess with these values
        REGISTRY.register(new BasicFishingBehavior.Factory(EntityType.SALMON, 32.0F, 1.0F, 0.6F));
        REGISTRY.register(new BasicFishingBehavior.Factory(EntityType.COD, 32.0F, 1.0F, 0.6F));
        REGISTRY.register(new BasicFishingBehavior.Factory(EntityType.PUFFERFISH, 32.0F, 1.0F, 0.6F));
    }

    @SubscribeEvent
    public static void onRegistryBuild(RegistryEvent.NewRegistry event)
    {
        new RegistryBuilder<FishingBehaviorFactory>()
                .setName(new ResourceLocation(MasterAngler.MODID, "fishing_behavior_factory"))
                .setType(FishingBehaviorFactory.class)
                .setDefaultKey(EntityType.SALMON.getRegistryName())
                .disableSaving()
                .disableSync()
                .create();
    }
}

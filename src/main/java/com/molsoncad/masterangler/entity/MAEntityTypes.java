package com.molsoncad.masterangler.entity;

import com.molsoncad.masterangler.MasterAngler;
import com.molsoncad.masterangler.client.renderer.entity.MasterFishingBobberRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class MAEntityTypes
{
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITIES, MasterAngler.MODID);

    public static final RegistryObject<EntityType<MasterFishingBobberEntity>> FISHING_BOBBER;

    static
    {
        FISHING_BOBBER = REGISTRY.register("fishing_bobber", () -> EntityType.Builder.<MasterFishingBobberEntity>createNothing(EntityClassification.MISC)
                .noSave()
                .noSummon()
                .setCustomClientFactory(MasterFishingBobberEntity::createFromSpawnPacket)
                .build(new ResourceLocation(MasterAngler.MODID, "fishing_bobber").toString()));
    }

    public static void register(IEventBus bus)
    {
        REGISTRY.register(bus);
    }

    public static void registerEntityRenderingHandlers()
    {
        RenderingRegistry.registerEntityRenderingHandler(FISHING_BOBBER.get(), MasterFishingBobberRenderer::new);
    }
}

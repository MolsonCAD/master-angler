package com.molsoncad.masterangler;

import com.molsoncad.masterangler.capability.CapabilityFishing;
import com.molsoncad.masterangler.capability.CapabilityProviderFishing;
import com.molsoncad.masterangler.capability.FishingProperties;
import com.molsoncad.masterangler.client.renderer.ModelHandler;
import com.molsoncad.masterangler.client.renderer.RenderHandler;
import com.molsoncad.masterangler.entity.MAEntityTypes;
import com.molsoncad.masterangler.loot.MALootConditions;
import com.molsoncad.masterangler.loot.MALootTables;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MasterAngler.MODID)
public class MasterAngler
{
    public static final String MODID = "masterangler";

    public MasterAngler()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::setupCommon);
        modEventBus.addListener(this::setupClient);
        modEventBus.register(MALootTables.class);
        modEventBus.register(ModelHandler.class);

        MAEntityTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setupCommon(FMLCommonSetupEvent event)
    {
        CapabilityFishing.register();
        event.enqueueWork(MALootConditions::register);
    }

    private void setupClient(FMLClientSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new RenderHandler(Minecraft.getInstance()));

        MAEntityTypes.registerEntityRenderingHandlers();
        event.enqueueWork(ModelHandler::registerModelProperties);
    }

    @SubscribeEvent
    public void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof LivingEntity)
        {
            event.addCapability(FishingProperties.ID, new CapabilityProviderFishing(FishingProperties::new));
        }
    }
}

package com.molsoncad.masterangler;

import com.molsoncad.masterangler.client.renderer.ModelHandler;
import com.molsoncad.masterangler.client.renderer.RenderHandler;
import com.molsoncad.masterangler.entity.MAEntityTypes;
import com.molsoncad.masterangler.entity.ai.behavior.FishingBehaviorFactories;
import com.molsoncad.masterangler.item.MAItems;
import com.molsoncad.masterangler.loot.MALootConditions;
import com.molsoncad.masterangler.loot.MALootTables;
import com.molsoncad.masterangler.network.PacketHandler;
import com.molsoncad.masterangler.util.MASoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MasterAngler.MODID)
public class MasterAngler
{
    public static final String MODID = "masterangler";

    private static MasterAngler instance;
    private RenderHandler renderHandler;

    public MasterAngler()
    {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        instance = this;

        modEventBus.addListener(this::setupCommon);
        modEventBus.addListener(this::setupClient);
        modEventBus.register(MAItems.class);
        modEventBus.register(MALootTables.class);
        modEventBus.register(MASoundEvents.class);
        modEventBus.register(ModelHandler.class);
        modEventBus.register(FishingBehaviorFactories.class);
        MAEntityTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        PacketHandler.register();
    }

    private void setupCommon(FMLCommonSetupEvent event)
    {
        event.enqueueWork(MALootConditions::register);
    }

    private void setupClient(FMLClientSetupEvent event)
    {
        renderHandler = new RenderHandler(Minecraft.getInstance());
        MinecraftForge.EVENT_BUS.register(renderHandler);

        MAEntityTypes.registerEntityRenderingHandlers();
        event.enqueueWork(ModelHandler::registerModelProperties);
    }

    public static MasterAngler getInstance()
    {
        return instance;
    }

    public RenderHandler getRenderHandler()
    {
        return renderHandler;
    }
}

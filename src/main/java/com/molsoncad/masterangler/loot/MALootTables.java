package com.molsoncad.masterangler.loot;

import com.molsoncad.masterangler.MasterAngler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MALootTables
{
    public static final ResourceLocation FISHING = new ResourceLocation(MasterAngler.MODID, "fishing");

    @SubscribeEvent
    public static void registerModifierSerializers(RegistryEvent.Register<GlobalLootModifierSerializer<?>> event)
    {
        event.getRegistry().register(new FishingLootModifier.Serializer().setRegistryName(MasterAngler.MODID, "fishing"));
    }
}

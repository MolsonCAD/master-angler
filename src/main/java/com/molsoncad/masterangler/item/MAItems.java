package com.molsoncad.masterangler.item;

import com.molsoncad.masterangler.MasterAngler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.item.Rarity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class MAItems
{
    private static final List<Item> ITEMS = new ArrayList<>();

    public static final Item FISHING_ROD = Items.FISHING_ROD;
    public static final Item BONE_FISHING_ROD = register("bone_fishing_rod", new TieredFishingRodItem(FishingRodTier.BONE, new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final Item BAMBOO_FISHING_ROD = register("bamboo_fishing_rod", new TieredFishingRodItem(FishingRodTier.BAMBOO, new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final Item SCUTE_FISHING_ROD = register("scute_fishing_rod", new TieredFishingRodItem(FishingRodTier.SCUTE, new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final Item CELESTIUM_FISHING_ROD = register("celestium_fishing_rod", new TieredFishingRodItem(FishingRodTier.CELESTIUM, new Item.Properties().tab(ItemGroup.TAB_TOOLS)));
    public static final Item CELESTIUM_FRAGMENT = register("celestium_fragment", new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final Item CELESTIUM_INGOT = register("celestium_ingot", new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final Item BLOOMING_HEART_OF_THE_SEA = register("blooming_heart_of_the_sea", new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS).rarity(Rarity.UNCOMMON)));

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        for (Item item : ITEMS)
        {
            event.getRegistry().register(item);
        }
    }

    private static Item register(String name, Item item)
    {
        ITEMS.add(item.setRegistryName(new ResourceLocation(MasterAngler.MODID, name)));
        return item;
    }
}

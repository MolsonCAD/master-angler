package com.molsoncad.masterangler.client.renderer;

import com.molsoncad.masterangler.MasterAngler;
import com.molsoncad.masterangler.item.MAItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;

public class ModelHandler
{
    public static void registerModelProperties()
    {
        registerFishingRodProperties(MAItems.BONE_FISHING_ROD);
        registerFishingRodProperties(MAItems.BAMBOO_FISHING_ROD);
        registerFishingRodProperties(MAItems.SCUTE_FISHING_ROD);
        registerFishingRodProperties(MAItems.CELESTIUM_FISHING_ROD);
    }

    private static void registerFishingRodProperties(Item item)
    {
        ItemModelsProperties.register(item, new ResourceLocation(MasterAngler.MODID, "cast"), (stack, world, living) -> {
            if (living instanceof PlayerEntity)
            {
                PlayerEntity player = (PlayerEntity) living;
                boolean mainhand = player.getMainHandItem() == stack;
                boolean offhand = player.getOffhandItem() == stack;

                if (player.getMainHandItem().getItem() instanceof FishingRodItem)
                {
                    offhand = false;
                }

                return (mainhand || offhand) && player.fishing != null ? 1.0F : 0.0F;
            }
            else
            {
                return 0.0F;
            }
        });
    }
}

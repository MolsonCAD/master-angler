package com.molsoncad.masterangler.client.renderer;

import com.molsoncad.masterangler.MasterAngler;
import com.molsoncad.masterangler.item.MAItems;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModelHandler
{
    private static final ResourceLocation FISHING_ROD_OVERRIDE_LOCATION = new ModelResourceLocation(MasterAngler.MODID + ":fishing_rod", "inventory");

    @SubscribeEvent
    public static void onModelRegistration(ModelRegistryEvent event)
    {
        ModelLoader.addSpecialModel(FISHING_ROD_OVERRIDE_LOCATION);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event)
    {
        IBakedModel fishingRodOverride = event.getModelRegistry().get(FISHING_ROD_OVERRIDE_LOCATION);
        event.getModelRegistry().put(new ModelResourceLocation("minecraft:fishing_rod", "inventory"), fishingRodOverride);
    }

    public static void registerModelProperties()
    {
        registerFishingRodProperties(MAItems.FISHING_ROD);
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

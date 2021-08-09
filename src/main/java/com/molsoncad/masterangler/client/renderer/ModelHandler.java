package com.molsoncad.masterangler.client.renderer;

import com.molsoncad.masterangler.MasterAngler;
import com.molsoncad.masterangler.item.IChargeableItem;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ModelHandler
{
    private static final ResourceLocation CUSTOM_FISHING_ROD_LOCATION = new ModelResourceLocation(MasterAngler.MODID + ":fishing_rod", "inventory");

    public static void registerModelProperties()
    {
        ItemModelsProperties.register(Items.FISHING_ROD, new ResourceLocation(MasterAngler.MODID, "charging"), (stack, world, living) -> {
            return living != null && living.isUsingItem() && living.getUseItem() == stack ? 1.0F : 0.0F;
        });

        // TODO: remove if "power" remains unused
        ItemModelsProperties.register(Items.FISHING_ROD, new ResourceLocation(MasterAngler.MODID, "power"), (stack, world, living) -> {
            if (living != null && living.getUseItem() == stack)
            {
                int ticks = stack.getUseDuration() - living.getUseItemRemainingTicks();
                return ((IChargeableItem) stack.getItem()).getPowerForTime(ticks);
            }
            else
            {
                return 0.0F;
            }
        });
    }

    @SubscribeEvent
    public static void onModelRegistration(ModelRegistryEvent event)
    {
        ModelLoader.addSpecialModel(CUSTOM_FISHING_ROD_LOCATION);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event)
    {
        IBakedModel fishingRodOverride = event.getModelRegistry().get(CUSTOM_FISHING_ROD_LOCATION);
        event.getModelRegistry().put(new ModelResourceLocation("minecraft:fishing_rod", "inventory"), fishingRodOverride);
    }
}

package com.molsoncad.masterangler.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.molsoncad.masterangler.item.IChargeableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderHandler
{
    private final Minecraft minecraft;
    private float fovModifier;

    public RenderHandler(Minecraft minecraft)
    {
        this.minecraft = minecraft;
        this.fovModifier = 1.0F;
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event)
    {
        PlayerEntity player = minecraft.player;
        ItemStack itemStack = event.getItemStack();
        Hand hand = event.getHand();

        if (player != null && player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand)
        {
            if (itemStack.getItem() instanceof FishingRodItem)
            {
                MatrixStack matrixStack = event.getMatrixStack();
                IRenderTypeBuffer buffer = event.getBuffers();
                boolean mainHand = hand == Hand.MAIN_HAND;
                float flip = mainHand ? 1.0F : -1.0F;
                float ticksInUse = (float) (itemStack.getUseDuration() - player.getUseItemRemainingTicks()) + event.getPartialTicks() - 1.0F;
                float power = ((IChargeableItem) itemStack.getItem()).getPowerForTime(ticksInUse);

                matrixStack.pushPose();

                applyItemArmTransform(matrixStack, mainHand, event.getEquipProgress());
                matrixStack.translate(flip * -0.1, power * 0.1, power * 0.1);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(power * 27.0F));
                matrixStack.mulPose(Vector3f.YN.rotationDegrees(flip * 5.0F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(flip * 2.0F));
                fovModifier = 1.0F - power * 0.1F;

                TransformType transformType = mainHand ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND;
                minecraft.getItemInHandRenderer().renderItem(player, itemStack, transformType, !mainHand, matrixStack, buffer, event.getLight());

                matrixStack.popPose();
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event)
    {
        PlayerEntity player = event.getEntity();

        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0)
        {
            event.setNewfov(event.getNewfov() * fovModifier);
        }
        else
        {
            fovModifier = 1.0F;
        }
    }

    private void applyItemArmTransform(MatrixStack matrixStack, boolean mainHand, double progress)
    {
        double flip = mainHand ? 1.0 : -1.0;
        matrixStack.translate(flip * 0.56, -0.52 - progress * 0.6, -0.72);
    }
}

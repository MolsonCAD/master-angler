package com.molsoncad.masterangler.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.molsoncad.masterangler.client.renderer.gui.FishingOverlayGui;
import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import com.molsoncad.masterangler.item.IChargeableItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderHandler
{
    private final Minecraft minecraft;
    private final FishingOverlayGui fishingOverlay;
    private float fovModifier;

    public RenderHandler(Minecraft minecraft)
    {
        this.minecraft = minecraft;
        this.fishingOverlay = new FishingOverlayGui(minecraft);
        this.fovModifier = 1.0F;
    }

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event)
    {
        PlayerEntity player = minecraft.player;
        MatrixStack matrixStack = event.getMatrixStack();
        ItemStack itemStack = event.getItemStack();

        matrixStack.pushPose();

        if (player != null && itemStack.getItem() instanceof FishingRodItem)
        {
            MasterFishingBobberEntity bobber = (MasterFishingBobberEntity) player.fishing;
            IRenderTypeBuffer buffer = event.getBuffers();
            Hand hand = event.getHand();
            boolean mainHand = hand == Hand.MAIN_HAND;
            float flip = mainHand ? 1.0F : -1.0F;

            if (bobber != null && bobber.isHooked())
            {
                float wiggle = MathHelper.sin(player.tickCount + event.getPartialTicks()) * 1.3F;

                applyItemArmTransform(matrixStack, mainHand, event.getEquipProgress());
                matrixStack.translate(flip * -0.15, wiggle * 0.004F, wiggle * 0.004F);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(wiggle + 12.0F));
                matrixStack.mulPose(Vector3f.YN.rotationDegrees(flip * 5.0F));
            }
            else if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand)
            {
                float ticksInUse = (float) (itemStack.getUseDuration() - player.getUseItemRemainingTicks()) + event.getPartialTicks() - 1.0F;
                float power = ((IChargeableItem) itemStack.getItem()).getPowerForTime(ticksInUse);

                fovModifier = 1.0F - power * 0.1F;

                applyItemArmTransform(matrixStack, mainHand, event.getEquipProgress());
                matrixStack.translate(flip * -0.1, power * 0.1, power * 0.1);
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(power * 40.0F - 12.0F));
                matrixStack.mulPose(Vector3f.YN.rotationDegrees(flip * 5.0F));
                matrixStack.mulPose(Vector3f.ZP.rotationDegrees(flip * 2.0F));
            }
            else
            {
                return;
            }

            TransformType transformType = mainHand ? TransformType.FIRST_PERSON_RIGHT_HAND : TransformType.FIRST_PERSON_LEFT_HAND;
            minecraft.getItemInHandRenderer().renderItem(player, itemStack, transformType, !mainHand, matrixStack, buffer, event.getLight());

            event.setCanceled(true);
        }

        matrixStack.popPose();
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

    @SubscribeEvent
    public void onPreRenderGameOverlay(RenderGameOverlayEvent.Pre event)
    {
        if (event.getType() == ElementType.BOSSHEALTH)
        {
            event.setCanceled(fishingOverlay.render(event.getMatrixStack(), event.getWindow()));
        }
    }

    private void applyItemArmTransform(MatrixStack matrixStack, boolean mainHand, double progress)
    {
        double flip = mainHand ? 1.0 : -1.0;
        matrixStack.translate(flip * 0.56, -0.52 - progress * 0.6, -0.72);
    }

    public FishingOverlayGui getFishingOverlay()
    {
        return fishingOverlay;
    }
}

package com.molsoncad.masterangler.client.renderer.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.molsoncad.masterangler.MasterAngler;
import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import com.molsoncad.masterangler.network.message.UpdateFishingInfoMessage;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

public class FishingOverlayGui extends AbstractGui
{
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(MasterAngler.MODID, "textures/gui/bars.png");
    private static final int PADDLE_WIDTH = 28;
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final int RED_BAR = BAR_HEIGHT * 2;
    private static final int GREEN_BAR = BAR_HEIGHT * 3;
    private static final int BAR_MARKS = 48;

    private final Minecraft minecraft;
    private float stamina;
    private float tension;

    public FishingOverlayGui(Minecraft minecraft)
    {
        this.minecraft = minecraft;
    }

    public boolean render(MatrixStack matrixStack, MainWindow window)
    {
        PlayerEntity player = minecraft.player;

        if (player != null && player.fishing instanceof MasterFishingBobberEntity && minecraft.gameMode != null)
        {
            MasterFishingBobberEntity bobber = (MasterFishingBobberEntity) player.fishing;

            if (bobber.isAlive() && bobber.isHooked())
            {
                int screenX = (window.getGuiScaledWidth() / 2) - (BAR_WIDTH / 2);
                int screenY = window.getGuiScaledHeight() - (BAR_HEIGHT * 2) - (minecraft.gameMode.canHurtPlayer() ? 51 : 41);

                RenderSystem.defaultBlendFunc();
                RenderSystem.enableBlend();
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                minecraft.getTextureManager().bind(TEXTURE_LOCATION);

                drawStaminaBar(matrixStack, screenX, screenY, stamina);
                drawTensionBar(matrixStack, screenX, screenY + BAR_HEIGHT + 1, tension);

                RenderSystem.disableBlend();
                return true;
            }
        }

        return false;
    }

    private void drawTensionBar(MatrixStack matrixStack, int screenX, int screenY, float progress)
    {
        int offset = (int) ((BAR_WIDTH - PADDLE_WIDTH) * progress);
        int bar = (progress < 0.12 || progress > 0.88) ? RED_BAR : GREEN_BAR;

        blit(matrixStack, screenX, screenY, 0, 0, BAR_WIDTH, BAR_HEIGHT);
        drawPaddle(matrixStack, screenX + offset, screenY, bar);
        blit(matrixStack, screenX, screenY, 0, BAR_MARKS, BAR_WIDTH, BAR_HEIGHT);
    }

    private void drawPaddle(MatrixStack matrixStack, int screenX, int screenY, int bar)
    {
        blit(matrixStack, screenX, screenY, 0, bar, 4, BAR_HEIGHT);
        blit(matrixStack, screenX + 4, screenY, 4, bar, PADDLE_WIDTH - 8, BAR_HEIGHT);
        blit(matrixStack, screenX + PADDLE_WIDTH - 4, screenY, BAR_WIDTH - 4, bar, 4, BAR_HEIGHT);
    }

    private void drawStaminaBar(MatrixStack matrixStack, int screenX, int screenY, float progress)
    {
        blit(matrixStack, screenX, screenY, 0, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        blit(matrixStack, screenX, screenY, 0, RED_BAR, (int) (BAR_WIDTH * progress), BAR_HEIGHT);
    }

    public void update(UpdateFishingInfoMessage message)
    {
        tension = message.getTension();
        stamina = message.getStamina();
    }
}

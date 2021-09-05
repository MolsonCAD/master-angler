package com.molsoncad.masterangler.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class MasterFishingBobberRenderer extends EntityRenderer<MasterFishingBobberEntity>
{
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/fishing_hook.png");
    private static final RenderType HOOK_RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

    private final int segments;

    public MasterFishingBobberRenderer(EntityRendererManager manager)
    {
        super(manager);
        segments = 16;
    }

    @Override
    public void render(MasterFishingBobberEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight)
    {
        // BEGIN RENDER LINE
        matrixStack.pushPose();

        // BEGIN RENDER HOOK
        matrixStack.pushPose();

        matrixStack.scale(0.5F, 0.5F, 0.5F);
        matrixStack.mulPose(entityRenderDispatcher.cameraOrientation());
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));

        MatrixStack.Entry entry = matrixStack.last();
        Matrix4f pose = entry.pose();
        Matrix3f normal = entry.normal();
        IVertexBuilder builder = buffer.getBuffer(HOOK_RENDER_TYPE);

        vertex(builder, pose, normal, 0.0F, 0.0F, 0.0F, 1.0F, packedLight);
        vertex(builder, pose, normal, 1.0F, 0.0F, 1.0F, 1.0F, packedLight);
        vertex(builder, pose, normal, 1.0F, 1.0F, 1.0F, 0.0F, packedLight);
        vertex(builder, pose, normal, 0.0F, 1.0F, 0.0F, 0.0F, packedLight);

        // END RENDER HOOK
        matrixStack.popPose();

        PlayerEntity player = entity.getPlayerOwner();

        if (player != null)
        {
            float animRot = MathHelper.sin(MathHelper.sqrt(player.getAttackAnim(partialTicks)) * (float) Math.PI);
            double playerX = MathHelper.lerp(partialTicks, player.xo, player.getX());
            double playerY = MathHelper.lerp(partialTicks, player.yo, player.getY()) + player.getEyeHeight();
            double playerZ = MathHelper.lerp(partialTicks, player.zo, player.getZ());
            double flip = player.getMainArm() == HandSide.RIGHT ? 1.0 : -1.0;

            if (!(player.getMainHandItem().getItem() instanceof FishingRodItem))
            {
                flip = -flip;
            }

            if (entityRenderDispatcher.options != null &&
                entityRenderDispatcher.options.getCameraType().isFirstPerson() &&
                player == Minecraft.getInstance().player)
            {
                double fov = entityRenderDispatcher.options.fov / 100.0;
                double xo = -0.45 + (entity.isHooked() ? 0.08 : 0.0);

                // This fov scaling is kinda silly, but it's good enough
                fov = (0.75 * fov * fov) + (0.15 * fov) + 0.1;

                Vector3d rod = new Vector3d(flip * fov * xo, fov * -0.045, 0.38)
                        .xRot(-MathHelper.lerp(partialTicks, player.xRotO, player.xRot) * DEG_TO_RAD)
                        .yRot(-MathHelper.lerp(partialTicks, player.yRotO, player.yRot) * DEG_TO_RAD)
                        .yRot(animRot * 0.5F)
                        .xRot(animRot * 0.7F);

                playerX += rod.x;
                playerY += rod.y;
                playerZ += rod.z;
            }
            else
            {
                float playerYaw = MathHelper.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * DEG_TO_RAD;
                double angleX = MathHelper.cos(playerYaw);
                double angleZ = MathHelper.sin(playerYaw);
                double length = flip * 0.35;

                playerX -= (angleX * length) + angleZ * 0.8;
                playerY -= 0.45;
                playerZ -= (angleZ * length) - angleX * 0.8;

                if (player.isCrouching())
                {
                    playerY -= 0.1875F;
                }
            }

            double entityX = MathHelper.lerp(partialTicks, entity.xo, entity.getX());
            double entityY = MathHelper.lerp(partialTicks, entity.yo, entity.getY()) + 0.25;
            double entityZ = MathHelper.lerp(partialTicks, entity.zo, entity.getZ());
            float deltaX = (float) (playerX - entityX);
            float deltaY = (float) (playerY - entityY);
            float deltaZ = (float) (playerZ - entityZ);
            boolean hooked = entity.isHooked();

            builder = buffer.getBuffer(RenderType.lines());
            pose = matrixStack.last().pose();

            for (int i = 0; i < segments; ++i)
            {
                lineVertex(builder, pose, deltaX, deltaY, deltaZ, (float) i / segments, hooked);
                lineVertex(builder, pose, deltaX, deltaY, deltaZ, (float) (i + 1) / segments, hooked);
            }
        }

        // END RENDER LINE
        matrixStack.popPose();

        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    private static void vertex(IVertexBuilder builder, Matrix4f pose, Matrix3f normal, float x, float y, float u, float v, int packedLight)
    {
        builder.vertex(pose, x - 0.5F, y - 0.5F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private static void lineVertex(IVertexBuilder builder, Matrix4f pose, float x, float y, float z, float part, boolean hooked)
    {
        float partY = hooked ? part : (part * part + part) * 0.5F;

        builder.vertex(pose, x * part, y * partY + 0.25F, z * part)
                .color(0, 0, 0, 255)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(MasterFishingBobberEntity entity)
    {
        return TEXTURE_LOCATION;
    }
}

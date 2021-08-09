package com.molsoncad.masterangler.entity.ai.controller;

import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.MathHelper;

public class FishMovementController extends MovementController
{
    public FishMovementController(AbstractFishEntity mob)
    {
        super(mob);
    }

    @Override
    public void tick()
    {
        if (mob.isEyeInFluid(FluidTags.WATER))
        {
            mob.setDeltaMovement(mob.getDeltaMovement().add(0.0, 0.005, 0.0));
        }

        if (operation == Action.MOVE_TO)
        {
            double dx = wantedX - mob.getX();
            double dy = wantedY - mob.getY();
            double dz = wantedZ - mob.getZ();
            double distanceSqr = dx * dx + dy * dy + dz * dz;

            if (distanceSqr < 0.025)
            {
                operation = Action.WAIT;
                return;
            }

            float wantedSpeed = (float) (speedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            mob.setSpeed(MathHelper.lerp(0.125F, mob.getSpeed(), wantedSpeed));

            if (dy != 0.0)
            {
                double forceY = mob.getSpeed() * (dy / Math.sqrt(distanceSqr)) * 0.1;
                mob.setDeltaMovement(mob.getDeltaMovement().add(0.0, forceY, 0.0));
            }

            if (dx != 0.0 || dz != 0.0)
            {
                float rot = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
                mob.yRot = rotlerp(mob.yRot, rot, 90.0F);
                mob.yBodyRot = mob.yRot;
            }
        }
        else
        {
            mob.setSpeed(0.0F);
        }
    }
}

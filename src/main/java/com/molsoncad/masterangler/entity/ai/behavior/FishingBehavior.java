package com.molsoncad.masterangler.entity.ai.behavior;

import com.molsoncad.masterangler.entity.ai.controller.FishingController;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

public abstract class FishingBehavior
{
    protected final AbstractFishEntity mob;

    public FishingBehavior(AbstractFishEntity mob)
    {
        this.mob = mob;
    }

    public abstract void tickHooked(FishingController controller, PlayerEntity player);

    public abstract void tickBiting(Vector3d origin);
}

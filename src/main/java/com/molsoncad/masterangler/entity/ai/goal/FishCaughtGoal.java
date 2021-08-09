package com.molsoncad.masterangler.entity.ai.goal;

import com.molsoncad.masterangler.capability.CapabilityFishing;
import com.molsoncad.masterangler.capability.IFishingProperties;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.LazyOptional;

import java.util.EnumSet;
import java.util.List;

public class FishCaughtGoal extends Goal
{
    private final AbstractFishEntity mob;
    private int life;
    private boolean hasLanded;

    public FishCaughtGoal(AbstractFishEntity mob, int life)
    {
        this.mob = mob;
        this.life = life;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        LazyOptional<IFishingProperties> capability = mob.getCapability(CapabilityFishing.FISHING_PROPERTIES);
        return !mob.isInWater() && capability.isPresent() && capability.orElseThrow(IllegalStateException::new).isCaught();
    }

    @Override
    public boolean canContinueToUse()
    {
        return !mob.isInWater();
    }

    @Override
    public boolean isInterruptable()
    {
        return false;
    }

    @Override
    public void start()
    {
        hasLanded = false;

        if (mob.getMoveControl().hasWanted())
        {
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
        }
    }

    @Override
    public void stop()
    {
        mob.getCapability(CapabilityFishing.FISHING_PROPERTIES).ifPresent((properties) -> properties.setCaught(false));
    }

    @Override
    public void tick()
    {
        List<BoatEntity> boats = mob.level.getEntitiesOfClass(BoatEntity.class, mob.getBoundingBox().inflate(0.2), null);

        mob.getNavigation().stop();

        if (!boats.isEmpty())
        {
            BoatEntity boat = boats.get(0);

            mob.setDeltaMovement(Vector3d.ZERO);
            mob.setPos(boat.getX(), boat.getY() + 1.0, boat.getZ());
            mob.kill();
        }
        else if (hasLanded)
        {
            if (--life < 0)
            {
                mob.kill();
            }

            mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.5, 1.0, 0.5));
        }
        else if (mob.verticalCollision)
        {
            hasLanded = true;
        }
    }
}

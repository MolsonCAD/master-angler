package com.molsoncad.masterangler.entity.ai.goal;

import com.molsoncad.masterangler.entity.IFishingProperties;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;
import java.util.List;

public class FishCaughtGoal extends Goal
{
    private final AbstractFishEntity mob;
    private int life;
    private int timeout;
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
        return ((IFishingProperties) mob).isCaught();
    }

    @Override
    public boolean canContinueToUse()
    {
        return !mob.isInWater() || timeout > 0;
    }

    @Override
    public boolean isInterruptable()
    {
        return false;
    }

    @Override
    public void start()
    {
        timeout = 30;
        hasLanded = false;

        if (mob.getMoveControl().hasWanted())
        {
            mob.getMoveControl().setWantedPosition(mob.getX(), mob.getY(), mob.getZ(), 0.0);
        }
    }

    @Override
    public void stop()
    {
        ((IFishingProperties) mob).setCaught(false);
    }

    @Override
    public void tick()
    {
        mob.getNavigation().stop();

        if (mob.isInWater())
        {
            --timeout;
        }
        else
        {
            List<BoatEntity> boats = mob.level.getEntitiesOfClass(BoatEntity.class, mob.getBoundingBox().inflate(0.2), null);
            timeout = 0;

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

                mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.25, 1.0, 0.25));
            }
            else if (mob.verticalCollision)
            {
                hasLanded = true;
            }
        }
    }
}

package com.molsoncad.masterangler.entity.ai.goal;

import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.EnumSet;
import java.util.List;

public class FishingGoal extends Goal
{
    private static final double CHARGE_SPEED_FACTOR = 1.25;

    protected final AbstractFishEntity mob;
    protected final double speedModifier;
    protected MasterFishingBobberEntity bobber;
    private int timeUntilTimeout;
    private boolean wasBiting;

    public FishingGoal(AbstractFishEntity mob, double speedModifier)
    {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.bobber = null;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        AxisAlignedBB aabb = mob.getBoundingBox().inflate(16.0);
        List<MasterFishingBobberEntity> bobbers = mob.level.getEntitiesOfClass(MasterFishingBobberEntity.class, aabb, (bobber) ->
                bobber.isAvailable() && mob.getRandom().nextDouble() < 0.33 && mob.canSee(bobber)
        );

        if (!bobbers.isEmpty())
        {
            bobber = bobbers.get(mob.getRandom().nextInt(bobbers.size()));
            bobber.setTarget(mob);

            return mob.getNavigation().moveTo(bobber, speedModifier);
        }

        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        boolean isBobberValid = bobber != null && bobber.isAlive() && bobber.getTarget() == mob;
        return (isBobberValid || wasBiting) && timeUntilTimeout > 0;
    }

    @Override
    public boolean isInterruptable()
    {
        return bobber == null || !bobber.isAlive() || bobber.isLuring();
    }

    @Override
    public void start()
    {
        timeUntilTimeout = 160;
        wasBiting = false;
    }

    @Override
    public void stop()
    {
        if (bobber != null)
        {
            bobber.reset();
            bobber = null;
        }
    }

    @Override
    public void tick()
    {
        if (bobber != null)
        {
            if (bobber.isAlive() && !bobber.isLuring())
            {
                return;
            }
            else if (mob.getNavigation().isDone() && !wasBiting)
            {
                biteHook();
            }
            else if (!mob.getNavigation().getTargetPos().equals(bobber.blockPosition()))
            {
                mob.getNavigation().moveTo(bobber, speedModifier);
            }

            --timeUntilTimeout;
        }

        if (wasBiting && isInterruptable())
        {
            Vector3d target = RandomPositionGenerator.getPos(mob, 6, 0);
            wasBiting = false;

            if (target != null)
            {
                mob.getNavigation().moveTo(target.x, target.y, target.z, speedModifier);
            }
        }
    }

    private void biteHook()
    {
        Vector3d bobberPos = bobber.position();

        mob.getMoveControl().setWantedPosition(bobberPos.x, bobberPos.y - 0.5, bobberPos.z, speedModifier * CHARGE_SPEED_FACTOR);

        if (mob.position().distanceToSqr(bobberPos) < 1.0)
        {
            ServerWorld world = (ServerWorld) mob.level;
            float bobberWidth = bobber.getBbWidth();
            int particleCount = 1 + (int) (bobberWidth * 20.0F);

            bobber.setBiting();
            wasBiting = true;

            bobber.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (float) (mob.getRandom().nextGaussian() * 0.4));
            world.sendParticles(ParticleTypes.BUBBLE, bobberPos.x, bobberPos.y + 0.5, bobberPos.z, particleCount, bobberWidth, 0.0F, bobberWidth, 0.2F);
            world.sendParticles(ParticleTypes.FISHING, bobberPos.x, bobberPos.y + 0.5, bobberPos.z, particleCount, bobberWidth, 0.0F, bobberWidth, 0.2F);
        }
    }
}

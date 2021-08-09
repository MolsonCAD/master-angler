package com.molsoncad.masterangler.entity.ai.goal;

import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class LureGoal extends Goal
{
    private static final double CHARGE_SPEED_FACTOR = 1.25;
    private static final double SPIN_SPEED_FACTOR = 1.5;

    protected final AbstractFishEntity mob;
    protected MasterFishingBobberEntity bobber;
    protected final double speedModifier;
    private int timeUntilTimeout;
    private int timeUntilSpinUpdate;
    private double spinRadius;
    private double spinDepth;
    private Vector3d spinPos;
    private boolean wasBiting;

    public LureGoal(AbstractFishEntity mob, double speedModifier)
    {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.bobber = null;
        this.spinPos = Vector3d.ZERO;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse()
    {
        AxisAlignedBB aabb = mob.getBoundingBox().inflate(16.0);
        List<MasterFishingBobberEntity> bobbers = mob.level.getEntitiesOfClass(MasterFishingBobberEntity.class, aabb, (bobber) ->
                bobber.isLuring() && mob.getRandom().nextDouble() < 0.33 && mob.canSee(bobber)
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
        boolean validBobber = bobber != null && bobber.isAlive() && bobber.getTarget() == mob;
        return (validBobber || wasBiting) && timeUntilTimeout > 0;
    }

    @Override
    public boolean isInterruptable()
    {
        return bobber == null || !bobber.isAlive() || !bobber.isBiting();
    }

    @Override
    public void start()
    {
        timeUntilTimeout = 100;
        timeUntilSpinUpdate = 10;
        spinRadius = 1.0;
        spinDepth = 0.5;
        wasBiting = false;
    }

    @Override
    public void stop()
    {
        if (bobber != null)
        {
            bobber.setBiting(false);
            bobber.setTarget(null);
            bobber = null;
        }
    }

    @Override
    public void tick()
    {
        if (bobber != null)
        {
            if (bobber.isAlive() && bobber.isBiting())
            {
                spin();
                return;
            }
            else if (mob.getNavigation().isDone())
            {
                --timeUntilTimeout;
                biteHook();
            }
            else if (!mob.getNavigation().getTargetPos().equals(bobber.blockPosition()))
            {
                mob.getNavigation().moveTo(bobber, speedModifier);
            }
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

    private void spin()
    {
        double dx = mob.getX() - spinPos.x;
        double dz = mob.getZ() - spinPos.z;
        float rot = (float) MathHelper.wrapDegrees(MathHelper.atan2(dz, dx) + (Math.PI / 9.0));

        if (--timeUntilSpinUpdate <= 0)
        {
            Random random = mob.getRandom();

            timeUntilSpinUpdate = random.nextInt(16) + 8;
            spinRadius = random.nextDouble() + 0.8;
            spinDepth = timeUntilSpinUpdate % 3 == 0 ? random.nextDouble() + 0.5 : spinDepth;
        }

        double wx = MathHelper.cos(rot) * spinRadius + spinPos.x;
        double wy = MathHelper.lerp(0.125, mob.getMoveControl().getWantedY(), spinPos.y - spinDepth);
        double wz = MathHelper.sin(rot) * spinRadius + spinPos.z;

        mob.getMoveControl().setWantedPosition(wx, wy, wz, speedModifier * SPIN_SPEED_FACTOR);
    }

    private void biteHook()
    {
        Vector3d bobberPos = bobber.position();

        mob.getMoveControl().setWantedPosition(bobberPos.x, bobberPos.y - 0.5, bobberPos.z, speedModifier * CHARGE_SPEED_FACTOR);

        if (mob.position().distanceToSqr(bobberPos) < 1.0)
        {
            bobber.setBiting(true);
            wasBiting = true;
            spinPos = bobberPos;
        }
    }
}

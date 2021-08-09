package com.molsoncad.masterangler.entity.ai.goal;

import com.molsoncad.masterangler.util.PositionGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.EnumSet;
import java.util.function.Predicate;

public abstract class AvoidEntityGoal<T extends Entity> extends Goal
{
    public static final Predicate<Entity> MOVING_NO_SPECTATORS = (entity) -> {
        double dx = entity.getX() - entity.xo;
        double dy = entity.getY() - entity.yo;
        double dz = entity.getZ() - entity.zo;
        double speedSqr = dx * dx + dy * dy + dz * dz;

        return EntityPredicates.NO_SPECTATORS.test(entity) && speedSqr > 0.001;
    };

    private static final double DETECT_HEIGHT = 3.0;

    protected final AbstractFishEntity mob;
    protected final Class<T> targetClass;
    protected final double maxDistance;
    protected final double sprintDistanceSqr;
    protected final double walkSpeedModifier;
    protected final double sprintSpeedModifier;
    protected final PathNavigator navigator;
    protected Path path;
    protected T target;

    protected AvoidEntityGoal(AbstractFishEntity mob, Class<T> targetClass, double maxDistance, double speedModifier)
    {
        this(mob, targetClass, maxDistance, 0.0, speedModifier, speedModifier);
    }

    protected AvoidEntityGoal(AbstractFishEntity mob, Class<T> targetClass, double maxDistance, double sprintDistance, double walkSpeedModifier, double sprintSpeedModifier)
    {
        this.mob = mob;
        this.targetClass = targetClass;
        this.maxDistance = maxDistance;
        this.sprintDistanceSqr = sprintDistance * sprintDistance;
        this.walkSpeedModifier = walkSpeedModifier;
        this.sprintSpeedModifier = sprintSpeedModifier;
        this.navigator = mob.getNavigation();
        setFlags(EnumSet.of(Flag.MOVE));
    }

    protected abstract T getNearestEntity(Class<T> clazz, double x, double y, double z, AxisAlignedBB bounds);

    @Override
    public boolean canUse()
    {
        target = getNearestEntity(targetClass, mob.getX(), mob.getY(), mob.getZ(), mob.getBoundingBox().inflate(maxDistance, DETECT_HEIGHT, maxDistance));

        if (target != null)
        {
            // TODO: choose position behind target when cornered
            Vector3d randomPos = PositionGenerator.getRandomPosAvoid(mob, 8, 1, target.position());

            if (randomPos != null)
            {
                path = navigator.createPath(new BlockPos(randomPos), 0);
                return path != null;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        return !navigator.isDone();
    }

    @Override
    public void start()
    {
        navigator.moveTo(path, walkSpeedModifier);
        mob.setSpeed((float) (walkSpeedModifier * mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
    }

    @Override
    public void stop()
    {
        target = null;
    }

    @Override
    public void tick()
    {
        if (mob.distanceToSqr(target) < sprintDistanceSqr)
        {
            mob.getNavigation().setSpeedModifier(sprintSpeedModifier);
        }
        else
        {
            mob.getNavigation().setSpeedModifier(walkSpeedModifier);
        }
    }
}

package com.molsoncad.masterangler.entity.ai.goal;

import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.function.Predicate;

public class AvoidProjectileGoal<T extends ProjectileEntity> extends AvoidEntityGoal<T>
{
    public static final Predicate<ProjectileEntity> MOVING_NO_BOBBER = (entity) ->
            !(entity instanceof FishingBobberEntity) && MOVING_NO_SPECTATORS.test(entity);

    private final Predicate<ProjectileEntity> predicate;

    public AvoidProjectileGoal(AbstractFishEntity mob, Class<T> targetClass, double maxDistance, double speedModifier)
    {
        this(mob, targetClass, maxDistance, speedModifier, MOVING_NO_SPECTATORS::test);
    }

    public AvoidProjectileGoal(AbstractFishEntity mob, Class<T> targetClass, double maxDistance, double speedModifier, Predicate<ProjectileEntity> predicate)
    {
        super(mob, targetClass, maxDistance, speedModifier);
        this.predicate = predicate;
    }

    @Override
    protected T getNearestEntity(Class<T> clazz, double x, double y, double z, AxisAlignedBB bounds)
    {
        List<T> projectiles = mob.level.getLoadedEntitiesOfClass(clazz, bounds, predicate);
        return projectiles.isEmpty() ? null : projectiles.get(mob.getRandom().nextInt(projectiles.size()));
    }
}

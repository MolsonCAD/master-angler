package com.molsoncad.masterangler.entity.ai.goal;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.function.Predicate;

public class AvoidLivingGoal<T extends LivingEntity> extends AvoidEntityGoal<T>
{
    private final EntityPredicate entityPredicate;

    public AvoidLivingGoal(AbstractFishEntity mob, Class<T> targetClass, double maxDistance, double sprintDistance, double walkSpeedModifier, double sprintSpeedModifier)
    {
        this(mob, targetClass, maxDistance, sprintDistance, walkSpeedModifier, sprintSpeedModifier, EntityPredicates.NO_SPECTATORS::test);
    }

    public AvoidLivingGoal(AbstractFishEntity mob, Class<T> targetClass, double maxDistance, double sprintDistance, double walkSpeedModifier, double sprintSpeedModifier, Predicate<LivingEntity> predicate)
    {
        super(mob, targetClass, maxDistance, sprintDistance, walkSpeedModifier, sprintSpeedModifier);
        this.entityPredicate = new EntityPredicate().range(maxDistance).selector(predicate);
    }

    @Override
    protected T getNearestEntity(Class<T> clazz, double x, double y, double z, AxisAlignedBB bounds)
    {
        return mob.level.getNearestLoadedEntity(clazz, entityPredicate, mob, x, y, z, bounds);
    }
}

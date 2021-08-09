package com.molsoncad.masterangler.mixin;

import com.molsoncad.masterangler.entity.ai.controller.FishMovementController;
import com.molsoncad.masterangler.entity.ai.goal.AvoidLivingGoal;
import com.molsoncad.masterangler.entity.ai.goal.AvoidProjectileGoal;
import com.molsoncad.masterangler.entity.ai.goal.FishCaughtGoal;
import com.molsoncad.masterangler.entity.ai.goal.LureGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.passive.WaterMobEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFishEntity.class)
public abstract class MixinAbstractFishEntity extends WaterMobEntity
{
    private static final float SPEED_FACTOR = 0.01F;

    private MixinAbstractFishEntity(EntityType<? extends AbstractFishEntity> type, World world)
    {
        super(type, world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(EntityType<? extends AbstractFishEntity> type, World world, CallbackInfo ci)
    {
        this.moveControl = new FishMovementController((AbstractFishEntity) (Object) this);
    }

    @Inject(method = "registerGoals", at = @At("HEAD"), cancellable = true)
    public void onRegisterGoals(CallbackInfo ci)
    {
        AbstractFishEntity self = (AbstractFishEntity) (Object) this;

        super.registerGoals();
        goalSelector.addGoal(0, new FishCaughtGoal(self, 32));
        goalSelector.addGoal(1, new PanicGoal(self, 3.0));
        goalSelector.addGoal(1, new AvoidProjectileGoal<>(self, ProjectileEntity.class, 4.0, 6.0, AvoidProjectileGoal.MOVING_NO_BOBBER));
        goalSelector.addGoal(2, new LureGoal(self, 2.0));
        goalSelector.addGoal(3, new AvoidLivingGoal<>(self, PlayerEntity.class, 6.0, 4.0, 1.0, 6.0));
        goalSelector.addGoal(6, new AbstractFishEntity.SwimGoal(self));

        ci.cancel();
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void onTravel(Vector3d movement, CallbackInfo ci)
    {
        if (isEffectiveAi() && isInWater())
        {
            moveRelative(getSpeed() * SPEED_FACTOR, movement);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));

            if (getTarget() == null)
            {
                setDeltaMovement(getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        }
        else
        {
            super.travel(movement);
        }

        ci.cancel();
    }
}

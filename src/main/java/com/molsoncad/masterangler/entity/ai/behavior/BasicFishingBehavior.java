package com.molsoncad.masterangler.entity.ai.behavior;

import com.molsoncad.masterangler.entity.ai.controller.FishingController;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class BasicFishingBehavior extends FishingBehavior
{
    private static final float TENSION_DEFAULT = 0.2F;
    private static final float REST_THRESHOLD = 0.2F;
    private static final float CLOSENESS_GAP_SQR = 9.0F;
    private static final float CLOSENESS_RANGE_SQR = 81.0F;

    private static final float SWIM_SPEED = 0.01F;
    private static final float SPIN_SPEED_MODIFIER = 3.0F;
    private static final float STRAFE_TILT = (float) (Math.PI / 12.0);

    protected final Random random;
    protected final float energy;
    protected final float power;
    protected final float recovery;
    protected float strength;
    private float strafeSpeed;
    private float strafeRot;
    private int fightTime;
    private int restTime;
    private int strafeUpdateTime;
    private int spinUpdateTime;
    private double spinRadius;
    private double spinDepth;
    private boolean wasReeling;

    public BasicFishingBehavior(AbstractFishEntity mob, float energy, float power, float recovery)
    {
        super(mob);
        this.random = mob.getRandom();
        this.energy = energy;
        this.power = power;
        this.recovery = recovery;
        this.strength = 0.5F;
        this.strafeRot = (float) (Math.PI/ 2.0);
        this.spinRadius = 1.0;
        this.spinDepth = 0.5;
    }

    @Override
    public void tickHooked(FishingController controller, PlayerEntity player)
    {
        Vector3d norm = new Vector3d(mob.getX() - player.getX(), 0.0, mob.getZ() - player.getZ()).normalize();
        float rot = (float) (MathHelper.atan2(norm.z, norm.x) - (Math.PI / 2.0));
        float stamina = controller.getStamina();
        float tension = controller.getTension();
        float closeness = getCloseness(player);
        float speed;

        if (controller.isReeling())
        {
            strength = getFightStrength(stamina, closeness);
            speed = -controller.getReelSpeed() * (1.0F - strength);
            wasReeling = true;

            stamina += (strength > REST_THRESHOLD ? -strength : (1.0F - strength / REST_THRESHOLD) * recovery) / energy;
            tension += Math.max((power * strength) / controller.getLineStrength(), tension < TENSION_DEFAULT ? controller.getDrainAmount() : 0.0F);
        }
        else
        {
            if (wasReeling)
            {
                wasReeling = false;
                mob.setDeltaMovement(Vector3d.ZERO);
            }

            strength = getFleeStrength(stamina, closeness);
            speed = SWIM_SPEED * strength;

            stamina += ((1.0F - strength * 0.5F) * recovery) / energy;
            tension -= controller.getDrainAmount();
        }

        if (--strafeUpdateTime <= 0)
        {
            strafeUpdateTime = random.nextInt(20 + (int) (20 * (1.0F - strength))) + 5;
            strafeSpeed = SWIM_SPEED * strength * (random.nextFloat() + 0.5F);
            strafeRot = -strafeRot;

            if (random.nextFloat() < 0.2F)
            {
                mob.playSound(SoundEvents.FISH_SWIM, 0.25F, 1.0F + (float) (mob.getRandom().nextGaussian() * 0.4));
                ((ServerWorld) mob.level).sendParticles(ParticleTypes.BUBBLE, mob.getX(), mob.getY() + 0.5, mob.getZ(), 4, 0.5, 0.0, 0.5, 0.2);
            }
        }

        if (!mob.level.getFluidState(mob.blockPosition().above()).is(FluidTags.WATER))
        {
            mob.setDeltaMovement(mob.getDeltaMovement().add(0.0, -0.01, 0.0));
        }

        rot += strafeRot < 0 ? STRAFE_TILT : -STRAFE_TILT;
        mob.setDeltaMovement(mob.getDeltaMovement().add(norm.scale(speed)).add(norm.yRot(strafeRot).scale(strafeSpeed)));
        mob.yRot = MathHelper.wrapDegrees(rot * (float) (180.0 / Math.PI));
        mob.yBodyRot = mob.yRot;

        controller.setStamina(MathHelper.clamp(stamina, 0.0F, 1.0F));
        controller.setTension(MathHelper.clamp(tension, 0.0F, 1.0F));
    }

    private float getCloseness(PlayerEntity player)
    {
        BlockRayTraceResult rayTraceResult = mob.level.clip(new RayTraceContext(
                mob.position(),
                new Vector3d(player.getX(), mob.getY(), player.getZ()),
                RayTraceContext.BlockMode.OUTLINE,
                RayTraceContext.FluidMode.NONE,
                mob));
        float distance = (float) player.distanceToSqr(mob);

        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK)
        {
            distance = (float) Math.min(distance, rayTraceResult.distanceTo(mob));
        }

        return 1.0F - MathHelper.clamp((distance - CLOSENESS_GAP_SQR) / CLOSENESS_RANGE_SQR, 0.0F, 1.0F);
    }

    protected float getFightStrength(float stamina, float closeness)
    {
        float target = strength;

        if (fightTime > 0)
        {
            target = Math.min(stamina + closeness * 0.5F, 1.0F);

            if (--fightTime == 0)
            {
                restTime = random.nextInt((int) (20 * (1.0F - stamina))) + 10;
            }
        }
        else if (restTime-- > 0 && closeness < 1.0F)
        {
            target = REST_THRESHOLD * stamina;
        }
        else
        {
            fightTime = random.nextInt(30) + 10;
        }

        return MathHelper.lerp(0.2F, strength, target);
    }

    protected float getFleeStrength(float stamina, float closeness)
    {
        return Math.max(stamina, closeness);
    }

    @Override
    public void tickBiting(Vector3d origin)
    {
        double dx = mob.getX() - origin.x;
        double dz = mob.getZ() - origin.z;
        float rot = (float) MathHelper.wrapDegrees(MathHelper.atan2(dz, dx) + (Math.PI / 9.0));

        if (--spinUpdateTime <= 0)
        {
            spinUpdateTime = random.nextInt(16) + 8;
            spinRadius = random.nextDouble() + 0.8;
            spinDepth = spinUpdateTime % 3 == 0 ? random.nextDouble() + 0.5 : spinDepth;
        }

        double wx = MathHelper.cos(rot) * spinRadius + origin.x;
        double wy = MathHelper.lerp(0.125, mob.getMoveControl().getWantedY(), origin.y - spinDepth);
        double wz = MathHelper.sin(rot) * spinRadius + origin.z;

        mob.getMoveControl().setWantedPosition(wx, wy, wz, SPIN_SPEED_MODIFIER);
    }

    public static class Factory extends FishingBehaviorFactory
    {
        private final float energy;
        private final float power;
        private final float recovery;

        public Factory(EntityType<?> type, float energy, float power, float recovery)
        {
            super(type);
            this.energy = energy;
            this.power = power;
            this.recovery = recovery;
        }

        @Override
        public FishingBehavior create(AbstractFishEntity mob)
        {
            return new BasicFishingBehavior(mob, energy, power, recovery);
        }
    }
}

package com.molsoncad.masterangler.entity;

import com.google.common.collect.ImmutableList;
import com.molsoncad.masterangler.client.audio.ReelingTickableSound;
import com.molsoncad.masterangler.entity.ai.controller.FishingController;
import com.molsoncad.masterangler.item.FishingRodTier;
import com.molsoncad.masterangler.item.ITieredFishingRodItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class MasterFishingBobberEntity extends FishingBobberEntity implements IEntityAdditionalSpawnData
{
    private static final DataParameter<Integer> DATA_TARGET_ENTITY = EntityDataManager.defineId(MasterFishingBobberEntity.class, DataSerializers.INT);
    private static final DataParameter<Integer> DATA_FISHING_STATE = EntityDataManager.defineId(MasterFishingBobberEntity.class, DataSerializers.INT);

    private static final List<ItemStack> DUMMY_LOOT = ImmutableList.of(new ItemStack(Items.SALMON));
    private static final Logger LOGGER = LogManager.getLogger();

    private AbstractFishEntity target;
    private FishingState fishingState;
    private Vector3d biteOrigin;
    private int timeUntilLuring;
    private int timeUntilEscape;
    private final FishingController fishingController;

    public MasterFishingBobberEntity(PlayerEntity player, World world, @Nullable ITieredFishingRodItem rod, int luck, int speed)
    {
        super(player, world, luck, speed);
        this.biteOrigin = Vector3d.ZERO;
        this.fishingController = new FishingController(this, rod == null ? FishingRodTier.WOOD : rod.getTier());
        reset();
    }

    public static MasterFishingBobberEntity createFromSpawnPacket(FMLPlayMessages.SpawnEntity packet, World world)
    {
        PacketBuffer buffer = packet.getAdditionalData();
        PlayerEntity player = world.getPlayerByUUID(buffer.readUUID());

        return new MasterFishingBobberEntity(Objects.requireNonNull(player), world, null, 0, 0);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();

        getEntityData().define(DATA_TARGET_ENTITY, 0);
        getEntityData().define(DATA_FISHING_STATE, 0);
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> parameter)
    {
        super.onSyncedDataUpdated(parameter);

        if (DATA_TARGET_ENTITY.equals(parameter))
        {
            int id = getEntityData().get(DATA_TARGET_ENTITY) - 1;
            Entity entity = id >= 0 ? level.getEntity(id) : null;

            setTargetRaw(entity instanceof AbstractFishEntity ? (AbstractFishEntity) entity : null);
        }
        else if (DATA_FISHING_STATE.equals(parameter))
        {
            FishingState state = FishingState.getValue(getEntityData().get(DATA_FISHING_STATE));
            PlayerEntity player = getPlayerOwner();

            if (state == FishingState.BITING)
            {
                biteOrigin = position();
            }
            else if (level.isClientSide() && player != null && state == FishingState.HOOKED && fishingState != FishingState.HOOKED)
            {
                Minecraft.getInstance().getSoundManager().play(new ReelingTickableSound((ClientPlayerEntity) player, this));
            }

            fishingState = state;
        }
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer)
    {
        buffer.writeUUID(Objects.requireNonNull(getOwner()).getUUID());
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData)
    {}

    @Override
    public void tick()
    {
        PlayerEntity player = getPlayerOwner();

        if (player != null && !shouldStopFishing(player) && currentState == State.BOBBING)
        {
            if (!level.isClientSide())
            {
                if (target != null)
                {
                    if (!target.isAlive() || (isBiting() && timeUntilEscape > 0 && --timeUntilEscape == 0))
                    {
                        reset();
                    }
                }
                else if (timeUntilLuring > 0)
                {
                    BlockPos above = blockPosition().above();
                    timeUntilLuring -= (random.nextFloat() < 0.25F && level.isRainingAt(above)) ? 2 : 1;

                    if (timeUntilLuring <= 0)
                    {
                        setFishingState(FishingState.LURING);
                    }
                }
            }

            if (target != null && (isBiting() || isHooked()))
            {
                Vector3d lookOffset = target.getLookAngle().scale(target.getBbWidth() - 0.2);
                Vector3d mouthPos = target.getEyePosition(1.0F).add(lookOffset);

                setPos(mouthPos.x, mouthPos.y + 0.2, mouthPos.z);

                if (!level.isClientSide())
                {
                    fishingController.tick();
                }

                return;
            }
        }

        super.tick();
    }

    @Override
    protected boolean canHitEntity(Entity entity)
    {
        return !(entity instanceof AbstractFishEntity) && super.canHitEntity(entity);
    }

    @Override
    protected void catchingFish(BlockPos pos)
    {
        // Overwrite vanilla catching logic.
    }

    @Override
    public int retrieve(ItemStack stack)
    {
        PlayerEntity player = getPlayerOwner();

        if (!level.isClientSide() && player != null)
        {
            if (target != null && isHooked())
            {
                IFishingProperties properties = (IFishingProperties) target;
                ExperienceOrbEntity orb = new ExperienceOrbEntity(player.level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, getExperience());
                Vector3d destination = player.position().add(0.0, player.isPassenger() ? 0.5 : 0.0, 0.0);
                double gravity = target.hasEffect(Effects.SLOW_FALLING) ? target.getAttributeBaseValue(ForgeMod.ENTITY_GRAVITY.get()) : target.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
                double decayXZ = 0.91;
                double decayY = 0.98;

                CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) player, stack, this, DUMMY_LOOT);

                target.setDeltaMovement(getLaunch(destination, decayXZ, decayY, gravity));
                properties.setCaught(true);
                properties.setLuck(luck);
                player.level.addFreshEntity(orb);
                player.awardStat(Stats.FISH_CAUGHT);

                remove();
                return 1;
            }
            else
            {
                return Math.max(super.retrieve(stack), fishingController.getTension() >= 1.0F ? 1 : 0);
            }
        }

        return 0;
    }

    /**
     * Calculates the delta movement launch vector for an entity, which will land it at a given destination.
     * The equations used are derived from the {@link LivingEntity} (and {@link ItemEntity}) mid-air
     * delta movement update logic, which boils down to:
     * <pre>
     * x = xo * decayXZ
     * y = (yo - gravity) * decayY
     * z = zo * decayXZ
     * </pre>
     * where (xo, yo, zo) is the last delta movement and (x, y, z) is the current delta movement. The
     * decay values are effectively the air resistance in each direction.<br>
     * <br>
     * <b>Warning:</b> entities using more sophisticated position update logic than the model
     * shown above (e.g. variable decay/gravity over time) may not arrive at the destination correctly.
     * @return The delta movement launch vector for an entity, aiming at a destination.
     * @author MolsonCAD
     */
    private Vector3d getLaunch(Vector3d destination, double decayXZ, double decayY, double gravity)
    {
        Vector3d distance = destination.subtract(position());

        double ticks = getTicksForDistance(distance.length());
        double speed = (1.0 - decayXZ) / (1.0 - Math.pow(decayXZ, ticks + 1.0));
        double geosum = (1.0 - Math.pow(decayY, ticks)) / (1.0 - decayY);

        double x = speed * distance.x;
        double y = (distance.y - ((-gravity * decayY) / (1.0 - decayY)) * (ticks - geosum)) / geosum;
        double z = speed * distance.z;

        return new Vector3d(x, y, z);
    }

    /**
     * Calculate the number of ticks an entity should take to fly over a given distance.<br>
     * <b>Note:</b> larger values for a given distance will create a steeper launch arc.
     */
    private static double getTicksForDistance(double distance)
    {
        return 6.0 * Math.log(Math.abs(distance) + 1.0);
    }

    protected int getExperience()
    {
        return random.nextInt(6) + 1;
    }

    public void reset()
    {
        setTarget(null);
        setFishingState(FishingState.IDLE);

        timeUntilLuring = MathHelper.nextInt(random, 100, 600 - lureSpeed * 100);
        timeUntilEscape = MathHelper.nextInt(random, 40, 80);

        if (!level.isClientSide())
        {
            LOGGER.debug("[BOBBER] timeUntilLuring: {}, timeUntilEscape: {}", timeUntilLuring, timeUntilEscape);
        }
    }

    public boolean isAvailable()
    {
        return fishingState == FishingState.LURING && target == null;
    }

    public boolean isLuring()
    {
        return fishingState == FishingState.LURING;
    }

    public boolean isHooked()
    {
        return fishingState == FishingState.HOOKED;
    }

    public void setHooked()
    {
        setFishingState(FishingState.HOOKED);
    }

    public boolean isBiting()
    {
        return fishingState == FishingState.BITING;
    }

    public void setBiting()
    {
        setFishingState(FishingState.BITING);
    }

    public Vector3d getBiteOrigin()
    {
        return biteOrigin;
    }

    public FishingController getFishingController()
    {
        return fishingController;
    }

    @Nullable
    public Entity getTarget()
    {
        return target;
    }

    public void setTarget(@Nullable AbstractFishEntity entity)
    {
        getEntityData().set(DATA_TARGET_ENTITY, entity != null ? entity.getId() + 1 : 0);
    }

    protected void setTargetRaw(@Nullable AbstractFishEntity entity)
    {
        if (target != null)
        {
            ((IFishingProperties) target).setFishing(false);
        }

        target = entity;
        fishingController.setFish(target);

        if (target != null)
        {
            ((IFishingProperties) target).setFishing(true);
        }
    }

    protected void setFishingState(FishingState state)
    {
        getEntityData().set(DATA_FISHING_STATE, state.getId());
    }

    @Override
    public void remove(boolean keepData)
    {
        super.remove(keepData);
        setTargetRaw(null);
    }

    @Override
    public EntityType<?> getType()
    {
        return MAEntityTypes.FISHING_BOBBER.get();
    }

    @Override
    public IPacket<?> getAddEntityPacket()
    {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    protected enum FishingState
    {
        IDLE(0),
        LURING(1),
        BITING(2),
        HOOKED(3);

        private static final FishingState[] VALUES = values();
        private final int id;

        FishingState(int id)
        {
            this.id = id;
        }

        public static FishingState getValue(int id)
        {
            return VALUES[id];
        }

        public int getId()
        {
            return id;
        }
    }
}

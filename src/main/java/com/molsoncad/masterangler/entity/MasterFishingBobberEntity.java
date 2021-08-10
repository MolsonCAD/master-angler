package com.molsoncad.masterangler.entity;

import com.google.common.collect.ImmutableList;
import com.molsoncad.masterangler.capability.CapabilityFishing;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
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
    private static final DataParameter<Boolean> DATA_LURING = EntityDataManager.defineId(MasterFishingBobberEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> DATA_BITING = EntityDataManager.defineId(MasterFishingBobberEntity.class, DataSerializers.BOOLEAN);

    private static final List<ItemStack> DUMMY_LOOT = ImmutableList.of(new ItemStack(Items.SALMON));
    private static final Logger LOGGER = LogManager.getLogger();

    protected ItemStack rod;
    protected Entity target;
    private boolean luring;
    private boolean biting;
    private int timeUntilLuring;
    private int timeUntilEscape;

    public MasterFishingBobberEntity(PlayerEntity player, World world, @Nullable ItemStack rod, int luck, int speed)
    {
        super(player, world, luck, speed);
        this.rod = rod;
        reset();
    }

    public static MasterFishingBobberEntity createFromSpawnPacket(FMLPlayMessages.SpawnEntity packet, World world)
    {
        PacketBuffer buffer = packet.getAdditionalData();
        PlayerEntity player = world.getPlayerByUUID(buffer.readUUID());

        // Ignore luck and speed since those aren't used on the client side.
        return new MasterFishingBobberEntity(Objects.requireNonNull(player), world, null, 0, 0);
    }

    @Override
    protected void defineSynchedData()
    {
        super.defineSynchedData();

        getEntityData().define(DATA_TARGET_ENTITY, 0);
        getEntityData().define(DATA_LURING, false);
        getEntityData().define(DATA_BITING, false);
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> parameter)
    {
        super.onSyncedDataUpdated(parameter);

        if (DATA_TARGET_ENTITY.equals(parameter))
        {
            int id = getEntityData().get(DATA_TARGET_ENTITY) - 1;

            setTargetRaw(null);

            if (id >= 0)
            {
                setTargetRaw(level.getEntity(id));
            }
        }

        if (DATA_LURING.equals(parameter))
        {
            luring = getEntityData().get(DATA_LURING);
        }

        if (DATA_BITING.equals(parameter))
        {
            biting = getEntityData().get(DATA_BITING);
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

        if (!level.isClientSide() && player != null && !isHoldingRod(player))
        {
            remove();
            return;
        }

        if (currentState == State.BOBBING)
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
                else if (timeUntilLuring > 0 && --timeUntilLuring == 0)
                {
                    LOGGER.debug("[BOBBER] LURING");
                    getEntityData().set(DATA_LURING, true);
                }
            }

            if (isBiting())
            {
                Vector3d lookOffset = target.getLookAngle().scale(target.getBbWidth() - 0.2);
                Vector3d mouthPos = target.getEyePosition(1.0F).add(lookOffset);

                setPos(mouthPos.x, mouthPos.y + 0.2, mouthPos.z);
                return;
            }
        }

        super.tick();
    }

    private boolean isHoldingRod(PlayerEntity player)
    {
        return player.getMainHandItem() == rod || player.getOffhandItem() == rod;
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
        int damage = 0;

        if (!level.isClientSide() && player != null)
        {
            if (isBiting())
            {
                ExperienceOrbEntity orb = new ExperienceOrbEntity(player.level, player.getX(), player.getY() + 0.5, player.getZ() + 0.5, getExperience());
                double gravity = 0.04;
                double decayXZ = 0.98;
                double decayY = 0.98;

                if (target instanceof LivingEntity)
                {
                    LivingEntity living = (LivingEntity) target;
                    gravity = living.hasEffect(Effects.SLOW_FALLING) ? 0.01 : living.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
                    decayXZ = 0.91;

                    CriteriaTriggers.FISHING_ROD_HOOKED.trigger((ServerPlayerEntity) player, stack, this, DUMMY_LOOT);
                }

                target.getCapability(CapabilityFishing.FISHING_PROPERTIES).ifPresent(properties -> properties.setCaught(true).setLuck(luck));
                target.setDeltaMovement(getLaunch(player.position(), decayXZ, decayY, gravity));
                player.level.addFreshEntity(orb);
                player.awardStat(Stats.FISH_CAUGHT);

                damage = 1;
                remove();
            }
            else
            {
                return super.retrieve(stack);
            }
        }

        return damage;
    }

    /**
     * Calculates the delta movement launch vector for an entity, which will land it at a given destination.
     * The equations used are derived from the {@link LivingEntity} (and {@link ItemEntity}) mid-air
     * position update logic, which boils down to:
     * <pre>
     * x = xo * decayXZ
     * y = (yo - gravity) * decayY
     * z = zo * decayXZ
     * </pre>
     * where (xo, yo, zo) is the last delta position and (x, y, z) is the current delta position. The
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

    protected void reset()
    {
        if (target != null)
        {
            target.getCapability(CapabilityFishing.FISHING_PROPERTIES).ifPresent(properties -> properties.setLuring(false));
        }

        setTarget(null);
        getEntityData().set(DATA_LURING, false);
        getEntityData().set(DATA_BITING, false);

        timeUntilLuring = MathHelper.nextInt(random, 100, 600 - lureSpeed * 100);
        timeUntilEscape = MathHelper.nextInt(random, 40, 80);

        if (!level.isClientSide())
        {
            LOGGER.debug("[BOBBER] timeUntilLuring: {}, timeUntilEscape: {}", timeUntilLuring, timeUntilEscape);
        }
    }

    public boolean isLuring()
    {
        return luring && target == null;
    }

    public boolean isBiting()
    {
        return biting && target != null;
    }

    public void setBiting(boolean biting)
    {
        getEntityData().set(DATA_BITING, biting);
    }

    @Nullable
    public Entity getTarget()
    {
        return target;
    }

    public void setTarget(@Nullable Entity entity)
    {
        getEntityData().set(DATA_TARGET_ENTITY, entity != null ? entity.getId() + 1 : 0);
    }

    protected void setTargetRaw(@Nullable Entity entity)
    {
        if (target != null)
        {
            target.getCapability(CapabilityFishing.FISHING_PROPERTIES).ifPresent(properties -> properties.setLuring(false));
        }

        target = entity;

        if (target != null)
        {
            target.getCapability(CapabilityFishing.FISHING_PROPERTIES).ifPresent(properties -> properties.setLuring(true));
        }
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
}

package com.molsoncad.masterangler.entity.ai.controller;

import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import com.molsoncad.masterangler.entity.ai.behavior.FishingBehavior;
import com.molsoncad.masterangler.entity.ai.behavior.FishingBehaviorFactories;
import com.molsoncad.masterangler.item.IFishingRodTier;
import com.molsoncad.masterangler.network.PacketHandler;
import com.molsoncad.masterangler.network.message.UpdateFishingInfoMessage;
import net.minecraft.entity.passive.fish.AbstractFishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public class FishingController
{
    private final MasterFishingBobberEntity bobber;
    private final float reelSpeed;
    private final float lineStrength;
    private final float drainAmount;
    private FishingBehavior behavior;
    private float stamina;
    private float tension;
    private boolean reeling;

    public FishingController(MasterFishingBobberEntity bobber, IFishingRodTier tier)
    {
        this.bobber = bobber;
        this.reelSpeed = tier.getReelSpeed();
        this.lineStrength = tier.getLineStrength();
        this.drainAmount = tier.getLineStrength() * 0.000625F;
    }

    public void tick()
    {
        if (bobber.isAlive() && behavior != null)
        {
            PlayerEntity player = bobber.getPlayerOwner();

            if (player != null)
            {
                if (bobber.isHooked())
                {
                    behavior.tickHooked(this, player);
                    updateFishingInfo(player);
                }
                else if (bobber.isBiting())
                {
                    behavior.tickBiting(bobber.getBiteOrigin());
                }
            }

            if (tension <= 0.0F || tension >= 1.0F)
            {
                bobber.reset();
            }
        }
    }

    public void setFish(@Nullable AbstractFishEntity fish)
    {
        if (fish == null)
        {
            behavior = null;
        }
        else
        {
            behavior = FishingBehaviorFactories.createFromInstance(fish);
            stamina = 1.0F;
            tension = 0.2F;
        }
    }

    public boolean canBeCaught()
    {
        return behavior != null && stamina <= 0.0F;
    }

    public float getStamina()
    {
        return stamina;
    }

    public void setStamina(float stamina)
    {
        this.stamina = stamina;
    }

    public float getTension()
    {
        return tension;
    }

    public void setTension(float tension)
    {
        this.tension = tension;
    }

    public float getReelSpeed()
    {
        return reelSpeed;
    }

    public float getLineStrength()
    {
        return lineStrength;
    }

    public float getDrainAmount()
    {
        return drainAmount;
    }

    public boolean isReeling()
    {
        return reeling;
    }

    public void setReeling(boolean reeling)
    {
        this.reeling = reeling;
    }

    private void updateFishingInfo(PlayerEntity player)
    {
        UpdateFishingInfoMessage message = new UpdateFishingInfoMessage(stamina, tension);
        PacketHandler.getChannel().send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
    }
}

package com.molsoncad.masterangler.client.audio;

import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import com.molsoncad.masterangler.util.MASoundEvents;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;

public class ReelingTickableSound extends TickableSound
{
    private final ClientPlayerEntity player;
    private final MasterFishingBobberEntity bobber;

    public ReelingTickableSound(ClientPlayerEntity player, MasterFishingBobberEntity bobber)
    {
        super(MASoundEvents.REELING, SoundCategory.NEUTRAL);
        this.player = player;
        this.bobber = bobber;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }

    @Override
    public void tick()
    {
        if (bobber.isAlive() && bobber.isHooked())
        {
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            pitch = player.isUnderWater() ? 0.8F : 1.0F;
        }
        else
        {
            stop();
        }
    }
}

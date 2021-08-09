package com.molsoncad.masterangler.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityFishing
{
    @CapabilityInject(IFishingProperties.class)
    public static Capability<IFishingProperties> FISHING_PROPERTIES = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IFishingProperties.class, new Capability.IStorage<IFishingProperties>()
        {
            @Override
            public INBT writeNBT(Capability<IFishingProperties> capability, IFishingProperties instance, Direction side)
            {
                CompoundNBT nbt = new CompoundNBT();
                nbt.putBoolean("caught", instance.isCaught());
                nbt.putBoolean("luring", instance.isLuring());
                nbt.putFloat("luck", instance.getLuck());
                return nbt;
            }

            @Override
            public void readNBT(Capability<IFishingProperties> capability, IFishingProperties instance, Direction side, INBT nbtIn)
            {
                CompoundNBT nbt = (CompoundNBT) nbtIn;
                instance.setCaught(nbt.getBoolean("caught"))
                        .setLuring(nbt.getBoolean("luring"))
                        .setLuck(nbt.getFloat("luck"));
            }
        }, FishingProperties::new);
    }
}

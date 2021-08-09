package com.molsoncad.masterangler.capability;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityProviderFishing implements ICapabilitySerializable<INBT>
{
    private final NonNullSupplier<IFishingProperties> supplier;
    private IFishingProperties instance;

    public CapabilityProviderFishing(NonNullSupplier<IFishingProperties> supplier)
    {
        this.supplier = supplier;
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        return CapabilityFishing.FISHING_PROPERTIES == cap ? (LazyOptional<T>) LazyOptional.of(this::getInstance) : LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT()
    {
        return CapabilityFishing.FISHING_PROPERTIES.writeNBT(getInstance(), null);
    }

    @Override
    public void deserializeNBT(INBT nbt)
    {
        CapabilityFishing.FISHING_PROPERTIES.readNBT(getInstance(), null, nbt);
    }

    @Nonnull
    private IFishingProperties getInstance()
    {
        if (instance == null)
        {
            instance = supplier.get();
        }

        return instance;
    }
}

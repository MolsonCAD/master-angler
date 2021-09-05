package com.molsoncad.masterangler.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public interface IMessage
{
    void encode(PacketBuffer buffer);

    void process(Supplier<NetworkEvent.Context> contextSupplier);
}

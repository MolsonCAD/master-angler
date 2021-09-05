package com.molsoncad.masterangler.network;

import com.molsoncad.masterangler.MasterAngler;
import com.molsoncad.masterangler.network.message.IMessage;
import com.molsoncad.masterangler.network.message.UpdateFishingInfoMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Function;

public class PacketHandler
{
    public static final String PROTOCOL_VERSION = "1.0";

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MasterAngler.MODID, "channel.main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int id = 0;

    public static SimpleChannel getChannel()
    {
        return CHANNEL;
    }

    public static void register()
    {
        registerMessage(UpdateFishingInfoMessage.class, UpdateFishingInfoMessage::decode, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static <T extends IMessage> void registerMessage(Class<T> type, Function<PacketBuffer, T> decoder, NetworkDirection direction)
    {
        CHANNEL.registerMessage(++id, type, T::encode, decoder, (message, context) -> {
            message.process(context);
            context.get().setPacketHandled(true);
        }, Optional.of(direction));
    }
}

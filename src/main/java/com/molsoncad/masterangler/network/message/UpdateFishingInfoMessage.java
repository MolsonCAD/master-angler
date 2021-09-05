package com.molsoncad.masterangler.network.message;

import com.molsoncad.masterangler.MasterAngler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateFishingInfoMessage implements IMessage
{
    private final float tension;
    private final float stamina;

    public UpdateFishingInfoMessage(float stamina, float tension)
    {
        this.stamina = stamina;
        this.tension = tension;
    }

    public static UpdateFishingInfoMessage decode(PacketBuffer buffer)
    {
        return new UpdateFishingInfoMessage(buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public void encode(PacketBuffer buffer)
    {
        buffer.writeFloat(stamina);
        buffer.writeFloat(tension);
    }

    @Override
    public void process(Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() -> MasterAngler.getInstance().getRenderHandler().getFishingOverlay().update(this));
    }

    public float getStamina()
    {
        return stamina;
    }

    public float getTension()
    {
        return tension;
    }
}

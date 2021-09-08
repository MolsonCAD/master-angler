package com.molsoncad.masterangler.util;

import com.molsoncad.masterangler.MasterAngler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class MASoundEvents
{
    private static final List<SoundEvent> SOUND_EVENTS = new ArrayList<>();

    public static final SoundEvent REELING = create("reeling");

    @SubscribeEvent
    public static void registerSoundEvents(RegistryEvent.Register<SoundEvent> event)
    {
        for (SoundEvent soundEvent : SOUND_EVENTS)
        {
            event.getRegistry().register(soundEvent);
        }
    }

    private static SoundEvent create(String name)
    {
        SoundEvent event = new SoundEvent(new ResourceLocation(MasterAngler.MODID, name));
        SOUND_EVENTS.add(event.setRegistryName(event.getLocation()));
        return event;
    }
}

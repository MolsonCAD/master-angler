package com.molsoncad.masterangler.loot;

import com.molsoncad.masterangler.MasterAngler;
import com.molsoncad.masterangler.loot.conditions.FishedEntity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;

public class MALootConditions
{
    private static final Map<ResourceLocation, LootConditionType> CONDITIONS = new HashMap<>();

    public static final LootConditionType FISHED_ENTITY = create("fished_entity", new FishedEntity.Serializer());

    public static void register()
    {
        CONDITIONS.forEach((key, condition) -> Registry.register(Registry.LOOT_CONDITION_TYPE, key, condition));
    }

    private static LootConditionType create(String name, ILootSerializer<? extends ILootCondition> serializer)
    {
        LootConditionType condition = new LootConditionType(serializer);
        CONDITIONS.put(new ResourceLocation(MasterAngler.MODID, name), condition);
        return condition;
    }
}

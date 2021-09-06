package com.molsoncad.masterangler.loot.conditions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.molsoncad.masterangler.entity.IFishingProperties;
import com.molsoncad.masterangler.loot.MALootConditions;
import net.minecraft.entity.Entity;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;

import java.util.Set;

public class FishedEntity implements ILootCondition
{
    private final LootContext.EntityTarget target;

    private FishedEntity(LootContext.EntityTarget target)
    {
        this.target = target;
    }

    @Override
    public LootConditionType getType()
    {
        return MALootConditions.FISHED_ENTITY;
    }

    @Override
    public Set<LootParameter<?>> getReferencedContextParams()
    {
        return ImmutableSet.of(target.getParam());
    }

    @Override
    public boolean test(LootContext context)
    {
        Entity entity = context.getParamOrNull(target.getParam());
        return entity instanceof IFishingProperties && ((IFishingProperties) entity).isCaught();
    }

    public static class Serializer implements ILootSerializer<FishedEntity>
    {
        @Override
        public void serialize(JsonObject json, FishedEntity condition, JsonSerializationContext context)
        {
            json.add("entity", context.serialize(condition.target));
        }

        @Override
        public FishedEntity deserialize(JsonObject json, JsonDeserializationContext context)
        {
            return new FishedEntity(JSONUtils.getAsObject(json, "entity", context, LootContext.EntityTarget.class));
        }
    }
}

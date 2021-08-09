package com.molsoncad.masterangler.loot;

import com.google.gson.JsonObject;
import com.molsoncad.masterangler.capability.CapabilityFishing;
import com.molsoncad.masterangler.capability.IFishingProperties;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.entity.player.ItemFishedEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FishingLootModifier extends LootModifier
{
    private final ResourceLocation specialLootTable;

    protected FishingLootModifier(ILootCondition[] conditionsIn, ResourceLocation specialLootTable)
    {
        super(conditionsIn);
        this.specialLootTable = specialLootTable;
    }

    @Nonnull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
    {
        Entity entity = context.getParamOrNull(LootParameters.THIS_ENTITY);

        if (entity != null)
        {
            Optional<IFishingProperties> capability = entity.getCapability(CapabilityFishing.FISHING_PROPERTIES).resolve();

            if (capability.isPresent())
            {
                IFishingProperties properties = capability.get();

                if (properties.isCaught())
                {
                    List<ItemStack> specialLoot = new ArrayList<>();

                    context = new LootContext.Builder(context)
                            .withLuck(properties.getLuck() + context.getLuck())
                            .create(LootParameterSets.ENTITY);
                    context.getLootTable(specialLootTable).getRandomItems(context, specialLoot::add);

                    if (!specialLoot.isEmpty())
                    {
                        generatedLoot = specialLoot;
                    }
                }
            }
        }

        // TODO: post a cancellable EntityFishedEvent
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<FishingLootModifier>
    {
        @Override
        public FishingLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions)
        {
            ResourceLocation specialLootTable = new ResourceLocation(JSONUtils.getAsString(object, "special_loot_table"));
            return new FishingLootModifier(conditions, specialLootTable);
        }

        @Override
        public JsonObject write(FishingLootModifier instance)
        {
            return makeConditions(instance.conditions);
        }
    }
}

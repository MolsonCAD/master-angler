package com.molsoncad.masterangler.mixin;

import com.molsoncad.masterangler.entity.MasterFishingBobberEntity;
import com.molsoncad.masterangler.item.IChargeableItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FishingRodItem.class)
public class MixinFishingRodItem extends Item implements IVanishable, IChargeableItem
{
    public MixinFishingRodItem(Item.Properties properties)
    {
        super(properties);
    }

    /**
     * Overwrite vanilla fishing rod behaviour.
     * @author MolsonCAD
     */
    @Overwrite
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand)
    {
        ItemStack stack = player.getItemInHand(hand);

        if (player.fishing == null)
        {
            player.startUsingItem(hand);
        }
        else
        {
            if (!world.isClientSide())
            {
                int damage = player.fishing.retrieve(stack);
                stack.hurtAndBreak(damage, player, (entity) -> entity.broadcastBreakEvent(hand));
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundCategory.NEUTRAL, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        }

        return ActionResult.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, World world, LivingEntity entity, int remaining)
    {
        if (entity instanceof PlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) entity;

            if (!world.isClientSide())
            {
                int luck = EnchantmentHelper.getFishingLuckBonus(stack);
                int speed = EnchantmentHelper.getFishingSpeedBonus(stack);
                float power = getPowerForTime(getUseDuration(stack) - remaining);
                MasterFishingBobberEntity bobber = new MasterFishingBobberEntity(player, world, stack, luck, speed);

                bobber.shootFromRotation(player, player.xRot, player.yRot, 0.0F, power * 2.0F, 2.0F);
                world.addFreshEntity(bobber);
            }

            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public UseAction getUseAnimation(ItemStack p_77661_1_)
    {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return getChargeDuration();
    }

    @Override
    public float getPowerForTime(float ticks)
    {
        // Uses a modified sigmoid function that aims to give a reasonable power curve
        // between [0,2) seconds (recall that 20 ticks == 1 second). For reference:
        //   0 ticks => 0.05
        //   15 ticks => 0.5
        //   30 ticks => 0.95
        return (float) (1.0 / (1.0 + Math.exp(3.0 - ticks / 5.0)));
    }
}

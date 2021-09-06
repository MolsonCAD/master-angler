package com.molsoncad.masterangler.mixin;

import com.molsoncad.masterangler.entity.IFishingProperties;
import net.minecraft.entity.ai.goal.FollowSchoolLeaderGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.fish.AbstractGroupFishEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FollowSchoolLeaderGoal.class)
public abstract class MixinFollowSchoolLeaderGoal extends Goal
{
    @Final
    @Shadow
    private AbstractGroupFishEntity mob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    public void onCanUse(CallbackInfoReturnable<Boolean> cir)
    {
        IFishingProperties properties = (IFishingProperties) mob;

        if (properties.isFishing() || properties.isCaught())
        {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canContinueToUse", at = @At("RETURN"), cancellable = true)
    public void onCanContinueToUse(CallbackInfoReturnable<Boolean> cir)
    {
        IFishingProperties properties = (IFishingProperties) mob;
        cir.setReturnValue(cir.getReturnValue() && !properties.isFishing() && !properties.isCaught());
    }
}

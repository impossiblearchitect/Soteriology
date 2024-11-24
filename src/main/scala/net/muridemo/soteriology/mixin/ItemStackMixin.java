package net.muridemo.soteriology.mixin;

import net.muridemo.soteriology.gear.util.GearHelpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.verdantartifice.primalmagick.common.tags.ItemTagsPM;
import net.muridemo.soteriology.gear.util.GearHelpers.*;


@Debug(export = true)
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
  @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true, expect = 1)
  private void onIs(TagKey<Item> tag, CallbackInfoReturnable<Boolean> callback) {
    if (tag == ItemTagsPM.WARDABLE_ARMOR)
      if (statics.isSGearWardable(this))
        callback.setReturnValue(true);
  }

//  @Inject(method = "m_204117_" /*is(Lnet/minecraft/world/tags/TagKey;)Z" /*"m_204117_"*/,
//    at = @At("HEAD"), cancellable = true, require = 1)
//  private void onIs(TagKey<Item> tag, CallbackInfoReturnable<Boolean> callback) {
//    if (tag == ItemTagsPM.WARDABLE_ARMOR)
//      if (this.isSGearWardable)
//        callback.setReturnValue(true);
//  }
}

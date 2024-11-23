package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

import com.verdantartifice.primalmagick.common.tags.ItemTagsPM
import net.muridemo.soteriology.gear.util.GearHelpers.*


@Debug(`export` = true)
@Mixin(Array(classOf[ItemStack]))
abstract class ItemStackMixin {
  this: ItemStack =>
  @Inject(method = Array("Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/tags/TagKey;)Z" /*"m_204117_"*/), at = Array(new At(value = "HEAD")), cancellable = true, require = 1)
  private def onIs(tag: TagKey[Item], callback: CallbackInfoReturnable[Boolean]): Unit = {
    if (tag == ItemTagsPM.WARDABLE_ARMOR)
      val sgearWardable = this.isSGearWardable
      if (sgearWardable) 
        callback.setReturnValue(true)
  }
}

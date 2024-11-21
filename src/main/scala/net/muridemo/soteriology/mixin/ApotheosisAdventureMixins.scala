package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

import com.google.common.collect.ImmutableSet

import dev.shadowsoffire.apotheosis.adventure.AdventureModule
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableBlock

import net.muridemo.soteriology.Soteriology
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

@Mixin(Array(classOf[AdventureModule]))
object ApotheosisAdventureMixins {
  @Redirect(
    method = Array("tiles"),
    at = new At(
      value = "INVOKE",
      target = "Lcom/google/common/collect/ImmutableSet;of",
      ordinal = 1
    )
  )
  private def adjustReforgingValidBlocks(b1: ReforgingTableBlock, b2: ReforgingTableBlock, callback: CallbackInfoReturnable[ImmutableSet[ReforgingTableBlock]]) = {
    callback.setReturnValue {
      ImmutableSet.builder[ReforgingTableBlock]().add(b1, b2, Soteriology.SUPERIOR_REFORGING_TABLE.get()).build()
    }
  }
}

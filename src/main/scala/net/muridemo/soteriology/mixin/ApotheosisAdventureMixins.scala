package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect

import com.google.common.collect.ImmutableSet

import dev.shadowsoffire.apotheosis.adventure.AdventureModule
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableBlock

import net.muridemo.soteriology.Soteriology
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.Debug

@Debug(`export` = true)
@Mixin(Array(classOf[AdventureModule]))
abstract class ApotheosisAdventureMixins {
  @Redirect(
    method = Array("tiles"),
    at = new At(
      value = "INVOKE",
      target = "Lcom/google/common/collect/ImmutableSet;of",
      remap = false,
      ordinal = 1
    ),
    require = 1
  )
  private def adjustReforgingValidBlocks(b1: Object, b2: Object) : ImmutableSet[ReforgingTableBlock] = {
    Soteriology.LOGGER.info("Appending soteriology:superior_reforging_table to ReforgingTableTile valid blocks")
    Soteriology.LOGGER.info(s"simple=$b1, normal=$b2")
    ImmutableSet.builder().add(b1, b2, Soteriology.SUPERIOR_REFORGING_TABLE.get()).build().asInstanceOf[ImmutableSet[ReforgingTableBlock]]
  }
}

package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

import org.objectweb.asm.Opcodes;

import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.player.ItemTooltipEvent

import com.verdantartifice.primalmagick.common.events.PlayerEvents
import com.verdantartifice.primalmagick.common.sources.Source
import com.verdantartifice.primalmagick.common.items.armor.WardingModuleItem

import net.silentchaos512.gear.util.TraitHelper
import net.silentchaos512.gear.util.GearHelper

import net.muridemo.soteriology.Soteriology
import net.muridemo.soteriology.gear.util.GearHelpers.*

@Debug(`export` = true)
@Mixin(Array(classOf[PlayerEvents]))
object PrimalMagickPlayerEventMixin {
  @Redirect(
    method = Array("handleWardRegeneration"),
    at = new At(
      value = "FIELD", 
      target = "Lcom/verdantartifice/primalmagick/common/items/armor/WardingModuleItem;REGEN_COST:I",
      opcode = Opcodes.GETSTATIC),
    require = 1
  )
  private def adjustWardRegenCost(player: ServerPlayer): Int = {
    Math.round(WardingModuleItem.REGEN_COST * player.passiveCostModifiers(Source.EARTH)).toInt
  }
}

package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
// import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
// import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import com.llamalad7.mixinextras.injector.ModifyExpressionValue
// import org.spongepowered.asm.mixin.injection.Inject
// import org.spongepowered.asm.mixin.injection.Redirect
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

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
// import net.muridemo.soteriology.util.Helpers.*
import net.muridemo.soteriology.util.Macros.*
import net.muridemo.soteriology.gear.util.GearHelpers.*

import scala.compiletime.*
import scala.compiletime.ops.any.*
import scala.compiletime.ops.string.*

@Debug(`export` = true)
@Mixin(Array(classOf[PlayerEvents]))
object PrimalMagickPlayerEventMixin {
  // inline def mkTarget = ${ inlineStringInterp('{Seq("intValue = ")})('{WardingModuleItem.REGEN_COST.toString}) }
  inline val TARGET = "intValue = " + constValue[ToString[WardingModuleItem.REGEN_COST.type]]

  @ModifyExpressionValue(
    method = Array("handleWardRegeneration"),
    at = Array(new At(
      value = "CONSTANT",
      args = Array(TARGET)//,
      // target = "Lcom/verdantartifice/primalmagick/common/items/armor/WardingModuleItem;REGEN_COST:I",
      // opcode = Opcodes.GETSTATIC,
      // args = Array("log = true"))),
    )),
    require = 1
  )
  private def adjustWardRegenCost(original: Int, player: ServerPlayer): Int = {
    Math.round(original * player.passiveCostModifiers(Source.EARTH)).toInt
  }
}

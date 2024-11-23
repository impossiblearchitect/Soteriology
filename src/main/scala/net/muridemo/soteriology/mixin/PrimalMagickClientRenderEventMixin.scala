package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.At.Shift
import org.spongepowered.asm.mixin.injection.Redirect
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
import com.llamalad7.mixinextras.injector.wrapoperation.Operation
import org.objectweb.asm.Opcodes

import net.minecraftforge.event.entity.player.ItemTooltipEvent
import com.verdantartifice.primalmagick.common.items.armor.IManaDiscountGear
import com.verdantartifice.primalmagick.client.events.ClientRenderEvents
import com.llamalad7.mixinextras.expression.Expression
import com.llamalad7.mixinextras.expression.Definition

@Debug(`export` = true)
@Mixin(Array(classOf[ClientRenderEvents]))
object PrimalMagickClientRenderEventMixin {
  @Definition(id = "IManaDiscountGear", `type` = Array(classOf[IManaDiscountGear]))
  @Expression(Array("? instanceof IManaDiscountGear"))
  @WrapOperation(method = Array("renderTooltip"), at = Array(new At(value = "MIXINEXTRAS:EXPRESSION")), require = 1)
  private def renderTooltipCheck(ref: Object, original: Operation[Boolean], event: ItemTooltipEvent): Boolean = {
    original.call(ref) && ref.asInstanceOf[IManaDiscountGear].getBestManaDiscount(event.getItemStack(), event.getEntity()) > 0
  }
}


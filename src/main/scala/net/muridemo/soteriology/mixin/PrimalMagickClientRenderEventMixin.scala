package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.At.Shift
import org.spongepowered.asm.mixin.injection.Redirect
import org.objectweb.asm.Opcodes

import net.minecraftforge.event.entity.player.ItemTooltipEvent
import com.verdantartifice.primalmagick.common.items.armor.IManaDiscountGear
import com.verdantartifice.primalmagick.client.events.ClientRenderEvents

@Debug(`export` = true)
@Mixin(Array(classOf[ClientRenderEvents]))
object PrimalMagickClientRenderEventMixin {
  @Redirect(
    method = Array("renderTooltip"),
    at = new At(value = "JUMP", opcode = Opcodes.IFEQ, shift = Shift.BEFORE),
    require = 1
  )
  private def renderTooltipCheck(ref: Object, c: Class[?], event: ItemTooltipEvent): Boolean = {
    ref.isInstanceOf[IManaDiscountGear] && ref.asInstanceOf[IManaDiscountGear].getBestManaDiscount(event.getItemStack(), event.getEntity()) > 0
  }
}


package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.At.Shift
import org.spongepowered.asm.mixin.injection.Redirect

import org.objectweb.asm.Opcodes;

import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.entity.player.ItemTooltipEvent

import com.verdantartifice.primalmagick.common.events.PlayerEvents
import com.verdantartifice.primalmagick.common.events.CapabilityEvents
import com.verdantartifice.primalmagick.common.sources.Source
import com.verdantartifice.primalmagick.common.capabilities.ManaStorage
import com.verdantartifice.primalmagick.common.items.armor.IManaDiscountGear
import com.verdantartifice.primalmagick.client.events.ClientRenderEvents
import net.silentchaos512.gear.util.TraitHelper
import net.silentchaos512.gear.util.GearHelper

import net.muridemo.soteriology.Soteriology
import net.muridemo.soteriology.gear.util.GearHelpers.*
import com.verdantartifice.primalmagick.common.items.armor.WardingModuleItem

@Mixin(Array(classOf[PlayerEvents], classOf[CapabilityEvents], classOf[ClientRenderEvents]))
object PrimalMagickEventMixins {
  @Redirect(
    method = Array("attachItemStackCapability"),
    at = new At(value = "NEW", target = "Lcom/verdantartifice/primalmagick/common/capabilities/ManaStorage/Provider;")
  )
  private def adjustManaCapacity(
      capacity: Int, maxReceive: Int, maxExtract: Int, allowedSources: Array[Source], 
      event: AttachCapabilitiesEvent[ItemStack]): ManaStorage.Provider = {
    val itemStack = event.getObject()
    if (GearHelper.isGear(itemStack))
      val cap = capacity * (TraitHelper.getTraitLevel(itemStack, Soteriology.WARDABLE_ID) - 1)
      new ManaStorage.Provider(cap, maxReceive, maxExtract, allowedSources*)
    else
      new ManaStorage.Provider(capacity, maxReceive, maxExtract, allowedSources*)
  }

  @Redirect(
    method = Array("handleWardRegeneration"),
    at = new At(
      value = "FIELD", 
      target = "Lcom/verdantartifice/primalmagick/common/items/armor/WardingModuleItem;REGEN_COST:I",
      opcode = Opcodes.GETSTATIC)
  )
  private def adjustWardRegenCost(player: ServerPlayer): Int = {
    Math.round(WardingModuleItem.REGEN_COST * player.passiveCostModifiers(Source.EARTH)).toInt
  }

  @Redirect(
    method = Array("renderTooltip"),
    at = new At(value = "JUMP", opcode = Opcodes.IFEQ, shift = Shift.BEFORE)
  )
  private def renderTooltipCheck(ref: Object, c: Class[?], event: ItemTooltipEvent): Boolean = {
    ref.isInstanceOf[IManaDiscountGear] && ref.asInstanceOf[IManaDiscountGear].getBestManaDiscount(event.getItemStack(), event.getEntity()) > 0
  }
}

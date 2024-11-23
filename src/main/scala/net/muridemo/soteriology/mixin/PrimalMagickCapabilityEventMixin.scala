package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation

import net.minecraft.world.item.ItemStack
import net.minecraftforge.event.AttachCapabilitiesEvent

import com.verdantartifice.primalmagick.common.sources.Source
import com.verdantartifice.primalmagick.common.capabilities.ManaStorage
import com.verdantartifice.primalmagick.common.events.CapabilityEvents

import net.silentchaos512.gear.util.GearHelper
import net.silentchaos512.gear.util.TraitHelper

import net.muridemo.soteriology.Soteriology
import com.llamalad7.mixinextras.injector.wrapoperation.Operation

@Debug(`export` = true)
@Mixin(Array(classOf[CapabilityEvents]))
object PrimalMagickCapabilityEventMixin {
  @WrapOperation(
    method = Array("attachItemStackCapability"),
    at = Array(new At(
      value = "NEW", 
      target = "Lcom/verdantartifice/primalmagick/common/capabilities/ManaStorage/Provider;", 
      args = Array("log = true"))),
    require = 1
  )
  private def adjustManaCapacity(
      capacity: Int, maxReceive: Int, maxExtract: Int, allowedSources: Array[Source], 
      original: Operation[ManaStorage.Provider],
      event: AttachCapabilitiesEvent[ItemStack]) = {
    val itemStack = event.getObject()
    if (GearHelper.isGear(itemStack))
      val cap = capacity * (TraitHelper.getTraitLevel(itemStack, Soteriology.WARDABLE_ID) - 1)
      original.call(cap, maxReceive, maxExtract, allowedSources)
    else
      original.call(capacity, maxReceive, maxExtract, allowedSources)
  }
}

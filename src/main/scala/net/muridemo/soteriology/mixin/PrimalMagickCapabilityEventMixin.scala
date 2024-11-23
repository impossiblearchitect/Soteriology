package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Debug
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

import net.minecraft.world.item.ItemStack
import net.minecraftforge.event.AttachCapabilitiesEvent

import com.verdantartifice.primalmagick.common.sources.Source
import com.verdantartifice.primalmagick.common.capabilities.ManaStorage
import com.verdantartifice.primalmagick.common.events.CapabilityEvents

import net.silentchaos512.gear.util.GearHelper
import net.silentchaos512.gear.util.TraitHelper

import net.muridemo.soteriology.Soteriology

@Debug(`export` = true)
@Mixin(Array(classOf[CapabilityEvents]))
object PrimalMagickCapabilityEventMixin {
  @Inject(
    method = Array("attachItemStackCapability"),
    at = Array(new At(value = "NEW", target = "Lcom/verdantartifice/primalmagick/common/capabilities/ManaStorage/Provider;")),
    cancellable = true,
    require = 1
  )
  private def adjustManaCapacity(
      capacity: Int, maxReceive: Int, maxExtract: Int, allowedSources: Array[Source], 
      event: AttachCapabilitiesEvent[ItemStack],
      callback: CallbackInfoReturnable[ManaStorage.Provider]) = {
    val itemStack = event.getObject()
    if (GearHelper.isGear(itemStack))
      val cap = capacity * (TraitHelper.getTraitLevel(itemStack, Soteriology.WARDABLE_ID) - 1)
      callback.setReturnValue(ManaStorage.Provider(cap, maxReceive, maxExtract, allowedSources*))
  }
}

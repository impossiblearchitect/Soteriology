package net.muridemo.soteriology.mixin

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.{Inject, At}
import org.spongepowered.asm.mixin.injection.points.MethodHead
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

import net.minecraft.world.item.ItemStack
import net.minecraft.resources.ResourceLocation

import net.silentchaos512.gear.util.TraitHelper

@Mixin(Array(classOf[TraitHelper]))
object TraitHelperMixin {
  private val Subtrait = "(?<ns>\\w+):(?<tr>\\w+)/subtrait/(?<idx>\\d+)".r
  @Inject(method = Array("hasTrait"), at = Array(new At(value = "HEAD")), cancellable = true, require = 1)
  private def onHasTrait(gear: ItemStack, traitId: ResourceLocation, callback: CallbackInfoReturnable[Boolean]) = {
    println(s"onHasTrait: gear=$gear, traitId=$traitId")
    println(s"matched=${traitId.toString() match {case Subtrait(ns, tr, idx) => s"ns=$ns, tr=$tr, idx=$idx"}}")
    traitId.toString() match
      case Subtrait(ns, tr, idx) =>
        // Evaluate as if called on the parent trait
        callback.setReturnValue(TraitHelper.hasTrait(gear, new ResourceLocation(ns, tr)))
  }

  @Inject(method = Array("getTraitLevel"), at = Array(new At(value = "HEAD")), cancellable = true, require = 1)
  private def onGetTraitLevel(gear: ItemStack, traitId: ResourceLocation, callback: CallbackInfoReturnable[Int]) = {
    println(s"onHasTrait: gear=$gear, traitId=$traitId")
    println(s"matched=${traitId.toString() match {case Subtrait(ns, tr, idx) => s"ns=$ns, tr=$tr, idx=$idx"}}")
    traitId.toString() match
      case Subtrait(ns, tr, idx) =>
        // Evaluate as if called on the parent trait
        callback.setReturnValue(TraitHelper.getTraitLevel(gear, new ResourceLocation(ns, tr)))
  }
  
}

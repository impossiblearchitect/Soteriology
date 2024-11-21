package net.muridemo.soteriology.data

import java.util.Collection
import scala.jdk.CollectionConverters.*
import scala.collection.immutable.ArraySeq

import net.minecraft.data.DataGenerator
import net.muridemo.soteriology.Soteriology

import net.silentchaos512.gear.api.data.`trait`.TraitsProviderBase
import net.silentchaos512.gear.api.data.`trait`.TraitBuilder
import net.silentchaos512.gear.api.data.`trait`.SynergyTraitBuilder
import net.silentchaos512.gear.util.Const
import net.silentchaos512.gear.api.item.GearType

class TraitsProvider(val gen: DataGenerator) extends TraitsProviderBase(gen, Soteriology.MOD_ID) {
  override def getTraits(): Collection[TraitBuilder] = ArraySeq(
      TraitBuilder.simple(Soteriology.WARDABLE_ID, 5).withGearTypeCondition(GearType.ARMOR),

      SynergyTraitBuilder(Soteriology.HARMONIC_ID, 5, 0.05f).cancelsWith(Const.Traits.CRUDE)
    ).asJava
  
}
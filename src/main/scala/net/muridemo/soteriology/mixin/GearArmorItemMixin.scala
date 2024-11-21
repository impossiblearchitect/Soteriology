package net.muridemo.soteriology.mixin


import java.util.Optional
import scala.jdk.OptionConverters.*

import org.spongepowered.asm.mixin.Mixin

import net.minecraft.world.item.ItemStack
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.armortrim.ArmorTrim

import net.silentchaos512.gear.item.gear.GearArmorItem

import com.verdantartifice.primalmagick.common.items.armor.IManaDiscountGear
import com.verdantartifice.primalmagick.common.sources.Source
import com.verdantartifice.primalmagick.common.runes.SourceRune
import com.verdantartifice.primalmagick.common.items.misc.RuneItem
import com.verdantartifice.primalmagick.common.armortrim.TrimPatternsPM

import com.hollingsworth.arsnouveau.api.mana.IManaDiscountEquipment
import com.hollingsworth.arsnouveau.api.spell.Spell

import net.silentchaos512.gear.util.GearData

import net.muridemo.soteriology.util.Helpers.nullable
import net.muridemo.soteriology.util.Helpers.nullable.?
import net.muridemo.soteriology.gear.util.GearHelpers.*
import net.muridemo.soteriology.Soteriology

@Mixin(Array(classOf[GearArmorItem]))
class GearArmorItemMixin extends IManaDiscountGear, IManaDiscountEquipment {

    override def getManaDiscount(stack: ItemStack, player: Player, source: Source) = {
      val manaDiscount = GearData.getStatInt(stack, Soteriology.MANA_EFFICIENCY)
      this.getAttunedSource(stack, player).toScala match 
        case Some(`source`) => 2 * manaDiscount
        case Some(_) => 0
        case None => manaDiscount
    }
    
    override def getBestManaDiscount(stack: ItemStack, player: Player) = {
      val manaDiscount = GearData.getStatInt(stack, Soteriology.MANA_EFFICIENCY)
      this.getAttunedSource(stack, player).toScala match 
        case Some(_) => 2 * manaDiscount
        case None => manaDiscount
    }

    override def getAttunedSource(stack: ItemStack, player: Player) = {
      Optional.ofNullable(nullable:
        val trim = ArmorTrim.getTrim(player.?.level().registryAccess(), stack)
        trim match
          case SourceTrim(source) => source
          case _ => null)
    }

    override def getManaDiscount(item: ItemStack, spell: Spell) = {
      val manaDiscount = GearData.getStat(item, Soteriology.MANA_EFFICIENCY) / 100f
      val cost = spell.getCost()
      Math.max(0, (manaDiscount * cost).round)
    }

}

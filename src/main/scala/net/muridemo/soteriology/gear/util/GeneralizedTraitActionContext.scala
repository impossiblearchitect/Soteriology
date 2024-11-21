package net.muridemo.soteriology.gear.util

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.silentchaos512.gear.api.traits.TraitActionContext
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.silentchaos512.gear.api.traits.ITrait
import net.silentchaos512.gear.api.item.ICoreArmor
import net.silentchaos512.gear.api.item.ICoreItem
import net.silentchaos512.gear.util.TraitHelper

import scala.jdk.CollectionConverters.*
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.AABB

import scala.util.boundary, boundary.break
import net.silentchaos512.gear.compat.curios.CuriosCompat
import net.minecraftforge.fml.ModList

class GeneralizedTraitActionContext(val self: Option[LivingEntity], val traitLevel: Int, val gear: ItemStack, val target: Option[Entity]) {
  def of(tac: TraitActionContext): GeneralizedTraitActionContext = {
    new GeneralizedTraitActionContext(Option(tac.getPlayer()), tac.getTraitLevel(), tac.getGear(), None)
  }

  /**Converts a GTAC to a TAC, asserting that the wielder is either null or a player. (Will throw an exception if not.)*/
  def assertPlayer(): TraitActionContext = {
    self match {
      case Some(player: Player) => new TraitActionContext(player, traitLevel, gear)
      case None => new TraitActionContext(null, traitLevel, gear)
      case _ => throw new IllegalStateException("Expected player, got " + self)
    }
  }

  def withSelf(self: LivingEntity): GeneralizedTraitActionContext = {
    new GeneralizedTraitActionContext(Option(self), traitLevel, gear, target)
  }
  def withTraitLevel(level: Int): GeneralizedTraitActionContext = {
    new GeneralizedTraitActionContext(self, level, gear, target)
  }
  def withGear(gear: ItemStack): GeneralizedTraitActionContext = {
    new GeneralizedTraitActionContext(self, traitLevel, gear, target)
  }
  def withTarget(target: Entity): GeneralizedTraitActionContext = {
    new GeneralizedTraitActionContext(self, traitLevel, gear, Some(target))
  }
  def withoutTarget: GeneralizedTraitActionContext = {
    new GeneralizedTraitActionContext(self, traitLevel, gear, None)
  }

  def gearType = gear.getItem().asInstanceOf[ICoreItem].getGearType()

  def countSetPieces[T <: ITrait](`trait`: T): Int = {
    if (!"armor".equals(gearType))
      return 1
    self match
      case Some(player) =>
        player.getArmorSlots().asScala
          .count(stack => stack.getItem().isInstanceOf[ICoreArmor] && TraitHelper.hasTrait(stack, `trait`))
      case None => 1 // Even if there's no `self`, if this method is being called at all, there must be at least one set piece
  }

  def countTotalLevel[T <: ITrait](`trait`: T): Int = {
    self match
      case Some(player) =>
        player.getArmorSlots().asScala.map(stack => TraitHelper.getTraitLevel(stack, `trait`)).sum + {
          if (!ModList.get().isLoaded("curios")) then 0 else
            CuriosCompat.getEquippedCurios(player).asScala.map(stack => TraitHelper.getTraitLevel(stack, `trait`)).sum
        }
      case None => 1 // Even if there's no `self`, if this method is being called at all, there must be at least one set piece
  }
  

  def getEntityInCrosshairs(reachDistance: Double): Option[Entity] = {
    self.flatMap {self =>
      val pos = self.getEyePosition()
      val look = self.getLookAngle()
      val boundingBox = AABB(pos, look)
      boundary:
        for (entity <- self.level.getEntities(self, boundingBox, e => e != self && e.isAlive() && e.isPickable()).asScala) {
          if (self.hasLineOfSight(entity)) 
            break(Some(entity))
        }
        None
    }
  }

  def withTargetEntityInCrosshairs(reachDistance: Double) =
    GeneralizedTraitActionContext(self, traitLevel, gear, getEntityInCrosshairs(reachDistance))
  
}

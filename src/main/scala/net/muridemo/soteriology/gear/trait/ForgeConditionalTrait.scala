package net.muridemo.soteriology.gear.`trait`

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.common.collect.Multimap

import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.util.GsonHelper
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraftforge.registries.ForgeRegistries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier

import net.silentchaos512.gear.gear.`trait`.SimpleTrait
import net.silentchaos512.gear.util.TraitHelper
import net.silentchaos512.lib.util.TimeUtils
import net.silentchaos512.utils.EnumUtils
import net.silentchaos512.gear.SilentGear
import net.silentchaos512.gear.api.item.GearType
import net.silentchaos512.gear.api.item.ICoreArmor
import net.silentchaos512.gear.api.item.ICoreItem
import net.silentchaos512.gear.api.traits.ITrait
import net.silentchaos512.gear.gear.`trait`.SimpleTrait.Serializer
import net.silentchaos512.gear.api.traits.TraitInstance
import net.silentchaos512.gear.api.traits.ITraitSerializer
import net.silentchaos512.gear.api.traits.TraitActionContext
import net.silentchaos512.gear.api.stats.ItemStat
import net.silentchaos512.gear.gear.`trait`.TraitManager
import net.silentchaos512.gear.gear.`trait`.TraitSerializers

import net.muridemo.soteriology.Soteriology
import net.muridemo.soteriology.util.Helpers.{given, *}
import net.muridemo.soteriology.util.ObfuscatedReflectivePredicate
import net.muridemo.soteriology.util.EventuallyConstant.*
import net.muridemo.soteriology.gear.util.GearHelpers.*
import net.muridemo.soteriology.gear.util.GeneralizedTraitActionContext
import net.muridemo.soteriology.Config

import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.collection.immutable.ArraySeq

import cats.Functor
import cats.implicits.toFunctorOps

class ForgeConditionalTrait (val id: ResourceLocation, val serializer: ITraitSerializer[? <: ForgeConditionalTrait]) 
extends SimpleTrait(id, serializer) {
  import ForgeConditionalTrait.*
  sealed abstract class Indexer(val getIndex: (context: GeneralizedTraitActionContext) => Int)
  case object TraitLevelIndexer extends Indexer(_.traitLevel - 1)
  case object PieceCountIndexer extends Indexer(_.countSetPieces(this) - 1)
  case object TotalLevelIndexer extends Indexer(_.countTotalLevel(this) - 1)
  
  class ConditionalTraitData[F[T] <: IndexedSeq[T] : Functor]
  (val `trait`: ITrait, val indexer: Indexer, val levels: F[Int], val conditions: F[Condition], val target: Targeter):
    def traits(gtac: GeneralizedTraitActionContext): Option[TraitInstance] = {
      val index = indexer.getIndex(gtac)
      if (conditions.ec(index)(gtac.self))
        Some(TraitInstance.of(`trait`, levels.ec(index)))
      else
        None
    }

  object ConditionalTraitData:
    def nextID = {
      var n = 0
      () => {
        val i = n
        n += 1
        i
      }
    }
    def read[F[X] <: IndexedSeq[X] : Functor, S, R, I, C, T]
      (
        parse: R => (S, I, F[Int], F[C], T), 
        map_trait: S => ITrait,
        map_index: I => Indexer, 
        map_condition: C => Condition, 
        map_target: T => Targeter
      )(r: R): ConditionalTraitData[F] = {
      val (s, i, l, c, t) = parse(r)
      ConditionalTraitData(map_trait(s), map_index(i), l, c.fmap(map_condition), map_target(t))
    }

    def write[F[X] <: IndexedSeq[X] : Functor, S, R, I, C, T]
      (
        serialize: (S, I, F[Int], F[C], T) => R, 
        map_trait: ITrait => S,
        map_index: Indexer => I,
        map_condition: Condition => C,
        map_target: Targeter => T
      )(data: ConditionalTraitData[F]): R = {
      serialize(map_trait(data.`trait`), map_index(data.indexer), data.levels, data.conditions.fmap(map_condition), map_target(data.target))
    }

    def deserializeJSON = {
      read(
        (json: JsonObject) => (
          json.get("trait"),
          GsonHelper.getAsString(json, "type"),
          if json.get("level").isJsonArray() 
            then json.getAsImmutableArray("level").map(_.getAsInt())
            else 
              ArraySeq(json.get("level").getAsInt()),
          if json.get("condition").isJsonArray() 
            then json.getAsImmutableArray("condition")
            else 
              ArraySeq(json.get("condition")),
          GsonHelper.getAsString(json, "target")
        ),
        t => {
          if t.isJsonPrimitive() then TraitManager.get(t.getAsString())
          else {
            val subtrait = TraitSerializers.deserialize(new ResourceLocation(s"$id/subtrait/${nextID()}"), t.getAsJsonObject())
            Soteriology.registerTrait(subtrait)
          }
        },
        {
          case "trait_level" => TraitLevelIndexer
          case "piece_count" => PieceCountIndexer
          case "total_level" => TotalLevelIndexer
          case e => throw new JsonParseException(s"Unknown conditional trait type '$e' for Forge conditional trait $id")
        },
        c => {
          if c.isJsonPrimitive() then Condition.fromString(c.getAsString())
          else Condition.deserializeJSON(c.getAsJsonObject())
        },
        {
          case "self" => Targeter.SELF
          case "struck_entity" => Targeter.STRUCK
          case "struck_by_entity" => Targeter.STRUCKBY
          case "look_entity" => Targeter.LOOK
          case t =>
            throw new JsonParseException(s"Unknown target $t for Forge conditional trait $id")
        }
      )
    }

    def readFromNetwork = read(
      (buf: FriendlyByteBuf) => {
          val anon = buf.readBoolean()
          val `trait` = 
            if anon then TraitSerializers.read(buf)
            else TraitManager.get(buf.readResourceLocation())
          val indexer = buf.readVarInt()
          val levels = ArraySeq.unsafeWrapArray(buf.readVarIntArray())
          val conditions = buf.readList(Condition.readBuf).asScala.toArray
          val target = buf.readVarInt()
          (`trait`, indexer, levels, ArraySeq.unsafeWrapArray(conditions), target)
        },
      identity,
      {
        case 0 => TraitLevelIndexer
        case 1 => PieceCountIndexer
        case 2 => TotalLevelIndexer
        case e => throw new JsonParseException(s"Unknown conditional trait type '$e' for Forge conditional trait $id")
      },
      identity,
      Targeter.fromOrdinal
    )

    def writeToNetwork[F[X] <: IndexedSeq[X] : Functor](buf: FriendlyByteBuf, data: ConditionalTraitData[F]) = write(
      (`trait`: ITrait, indexer: Int, levels: F[Int], conditions: F[Condition], target: Targeter) => {
          `trait`.getId().toString() match
            case s"$_ns:$_this/subtrait/$_id" => 
              buf.writeBoolean(true)
              TraitSerializers.write(`trait`, buf)
            case _ =>
              buf.writeBoolean(false)
          buf.writeVarInt(indexer)
          buf.writeVarIntArray(levels.toArray)
          buf.writeCollection(conditions.asJava, Condition.writeBuf)
          buf.writeVarInt(target.ordinal)
        },
      identity, 
      {
        case TraitLevelIndexer => 0
        case PieceCountIndexer => 1
        case TotalLevelIndexer => 2
      }, 
      identity, identity)(data)
    

  private var ctrait: Map[GearType, ConditionalTraitData[IndexedSeq]] = Map.empty 
  // private var traits: (GeneralizedTraitActionContext => Option[TraitInstance]) = (_ => None)
  // private var target: Target = Target.SELF

  // private var traits: Map[String, Map[Int, List[TraitInstance]]] = Map.empty
  // private var conditions: Map[String, Map[Int, (SELF => Boolean)]] = Map.empty

  def this(id: ResourceLocation) = this(id, ForgeConditionalTrait.SERIALIZER)

  private def forwardTrait(context: TraitActionContext): Option[(TraitActionContext, ITrait)] = {
    ctrait.getByGearType(context.gearType).flatMap(cdata => {
      cdata.traits(context.gtac).map{tInst => 
        val traitLevel = tInst.getLevel()
        val newContext = context.withTraitLevel(traitLevel)
        (newContext, tInst.getTrait())
      }
    })
  }

  override def onUpdate(context: TraitActionContext, isEquipped: Boolean): Unit = {
    //Early exits
    if (!isEquipped) return
    val player = context.getPlayer()
    if (player == null || player.tickCount % 10 != 0) return

    forwardTrait(context).foreach((c, t) => t.onUpdate(c, isEquipped))
  }

  override def onAttackEntity(context: TraitActionContext, target: LivingEntity, baseValue: Float): Float = {
    ctrait.getByGearType(context.gearType).flatMap(_.traits(context.gtac.withTarget(target)).map{tInst => 
      val traitLevel = tInst.getLevel()
      val newContext = context.withTraitLevel(traitLevel)
      tInst.getTrait().onAttackEntity(newContext, target, baseValue)
    }).getOrElse(baseValue)
  }

  override def onDurabilityDamage(context: TraitActionContext, damageTaken: Int): Float = {
    forwardTrait(context).map((c, t) => t.onDurabilityDamage(c, damageTaken)).getOrElse(damageTaken.toFloat)
  }

  //onRecalculatePre/Post?

  override def onGetStat(context: TraitActionContext, stat: ItemStat, value: Float, damageRatio: Float): Float = {
    forwardTrait(context).map((c, t) => t.onGetStat(c, stat, value, damageRatio)).getOrElse(value)
  }

  override def onGetAttributeModifiers(context: TraitActionContext, modifiers: Multimap[Attribute, AttributeModifier], slot: String): Unit = {
    forwardTrait(context).foreach((c, t) => t.onGetAttributeModifiers(c, modifiers, slot))
  }

  override def onItemUse(context: UseOnContext, traitLevel: Int): InteractionResult = {
    val player = Option(context.getPlayer())
    val gtac = GeneralizedTraitActionContext(player, traitLevel, context.getItemInHand(), None)
    ctrait.getByGearType(gtac.gearType).flatMap(_.traits(gtac).map{tInst => 
      val traitLevel = tInst.getLevel()
      tInst.getTrait().onItemUse(context, traitLevel)
    }).getOrElse(InteractionResult.PASS)
  }

  //TODO Check if this uses the trait level of the conditional trait, or of the trait it's forwarding to
  override def onItemSwing(stack: ItemStack, wielder: LivingEntity, traitLevel: Int): Unit = {
    // Handle struck entity as if it were look entity, to handle cases like Gaia Burst or other "sword projection" attacks
    val context = GeneralizedTraitActionContext(Option(wielder), traitLevel, stack, None)
    ctrait.getByGearType(context.gearType).map(_.traits(context.withTargetEntityInCrosshairs(Config.lookRange)).foreach{tInst => 
      val traitLevel = tInst.getLevel()
      tInst.getTrait().onItemSwing(stack, wielder, traitLevel)
    })
  }

  override def addLootDrops(context: TraitActionContext, stack: ItemStack): ItemStack = {
    forwardTrait(context).map((c, t) => t.addLootDrops(context, stack)).getOrElse(stack)
  }

}

object ForgeConditionalTrait {
  final val SERIALIZER : ITraitSerializer[ForgeConditionalTrait] = new Serializer[ForgeConditionalTrait](
    Soteriology.FORGE_CONDITIONAL_TRAIT,
    ForgeConditionalTrait.apply(_),
    ForgeConditionalTrait.deserializeJson,
    ForgeConditionalTrait.readFromNetwork,
    ForgeConditionalTrait.writeToNetwork
  )
  
  enum Targeter(val getTarget: (GeneralizedTraitActionContext) => Option[Entity]):
    case SELF extends Targeter(_.self)
    case STRUCK extends Targeter(_.target)
    case STRUCKBY extends Targeter(_.self.map(_.getLastHurtByMob()))
    case LOOK extends Targeter(_.getEntityInCrosshairs(Config.lookRange))
 
  enum Condition(val check: (Option[Entity]) => Boolean):
    case ALWAYS extends Condition(_ => true)
    case VALID extends Condition(_.isDefined)
    case NEVER extends Condition(_ => false)
    case Reflective(methodName: String) extends Condition({
      val pred: ObfuscatedReflectivePredicate[Entity] = ObfuscatedReflectivePredicate[Entity](methodName)
      (_.map(pred) == Some(true))})
    case NOT(condition: Condition) extends Condition(!condition(_))
    case AND(conditions: Condition*) extends Condition(e => conditions.forall(_(e)))
    case OR(conditions: Condition*) extends Condition(e => conditions.exists(_(e)))

    def apply(e: Option[Entity]): Boolean = check(e)
    override def toString(): String = {
      this match
        case ALWAYS => "always"
        case VALID => "valid"
        case NEVER => "never"
        case Reflective(methodName) => methodName
        case NOT(condition) => s"not($condition)"
        case AND(conditions*) => s"and(${conditions.mkString(",")})"
        case OR(conditions*) => s"or(${conditions.mkString(",")})"
    }
  object Condition:
    def fromString(s: String): Condition = {
      s match
        case "always" => ALWAYS
        case "valid" => VALID
        case "never" => NEVER
        case s"not($condition)" => NOT(fromString(condition))
        case s"and($conditions)" => AND(conditions.split(",").map(fromString)*)
        case s"or($conditions)" => OR(conditions.split(",").map(fromString)*)
        case _ => Reflective(s)
    }
    def readBuf(buf: FriendlyByteBuf): Condition = {
      buf.readVarInt() match
        case 0 => ALWAYS
        case 1 => VALID
        case 2 => NEVER
        case 3 => Reflective(buf.readUtf())
        case 4 => NOT(readBuf(buf))
        case 5 => AND(ArraySeq.fill(buf.readVarInt())(readBuf(buf))*)
        case 6 => OR(ArraySeq.fill(buf.readVarInt())(readBuf(buf))*)
    }
    def writeBuf(buf: FriendlyByteBuf, condition: Condition): FriendlyByteBuf = {
      condition match
        case ALWAYS => buf.writeVarInt(0)
        case VALID => buf.writeVarInt(1)
        case NEVER => buf.writeVarInt(2)
        case Reflective(methodName) => buf.writeVarInt(3).writeUtf(methodName)
        case NOT(condition) => buf.writeVarInt(4).writeVarInt(0)
        case AND(conditions*) => 
          buf.writeVarInt(5).writeVarInt(conditions.length)
          conditions.foreach(writeBuf(buf, _))
          buf
        case OR(conditions*) => 
          buf.writeVarInt(6).writeVarInt(conditions.length)
          conditions.foreach(writeBuf(buf, _))
          buf
    }
    def deserializeJSON(json: JsonObject): Condition = fromString(json.getAsString())

  /* 
    Schema: Follows the convention of e.g. WielderEffectTrait.
    {
      "type" : "soteriology:forge_conditional_trait",
      "max_level": 5,
      "name": { "translate": "trait.foo.bar"},
      "description": { "translate": "trait.foo.bar.desc"},
      "conditional_traits": {
        "tool": {
          "type": "trait_level"
          "trait": "silentgear:baz",
          "level": [1, 1, 2, 2, 3]
          "target": "self",
          "condition": ["isUnderWater", "isInWater", "isInWater", "isInWaterOrRain", "true"]
          }, ...
        }
      }
    }
    * `target` should be one of `self`, `struck`, `struckby` or `look`. 
      * `self` -- the entity wielding the gear; for all traits
      * `struck` -- the entity being hit; for melee weapons and projectiles
      * `struckby` -- the entity that hit the wielder; for armor traits
      * `look` -- the entity being looked at; for bows and curios
    * `type` should be one of `trait_level`, `piece_count`, or `total_level` (meaning the sum of all copies of the trait across all gear pieces),
      and determines the index of the level and condition arrays to use
    * in addition to the reflective properties provided by `ReflectivePredicate`, `condition` recognizes five special values:
      * "and"/"or"/"not" for logical operations
      * "always"/"never" for unconditional activation/deactivation
      * "valid" for a condition that is always true if the target is not null
   */

  def deserializeJson(`trait`: ForgeConditionalTrait, json: JsonObject) = {
    if(!json.has("conditional_traits")) 
      throw new JsonParseException(s"Forge conditional trait ${`trait`.getId()} is missing 'conditional_traits' object")
    
    val jsonCTraits = json.getAsJsonObject("conditional_traits")
    `trait`.ctrait = jsonCTraits.asMap().asScala.map(
      (gearType, jsonCTrait) => (GearType.get(gearType), `trait`.ConditionalTraitData.deserializeJSON(jsonCTrait.getAsJsonObject()))
    ).toMap
  }

  def readFromNetwork(`trait`: ForgeConditionalTrait, buf: FriendlyByteBuf) = {
    `trait`.ctrait = 
      buf.readMap[GearType, `trait`.ConditionalTraitData[IndexedSeq]](
        buf => GearType.get(buf.readUtf()), `trait`.ConditionalTraitData.readFromNetwork(_)
      ).asScala.toMap
  }

  def writeToNetwork(`trait`: ForgeConditionalTrait, buf: FriendlyByteBuf) = {
    buf.writeMap(`trait`.ctrait.asJava, (buf, name) => buf.writeUtf(name.getName()), `trait`.ConditionalTraitData.writeToNetwork)
  }
}
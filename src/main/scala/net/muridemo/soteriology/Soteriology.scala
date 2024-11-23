package net.muridemo.soteriology

import net.minecraft.client.Minecraft
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.{BlockItem, CreativeModeTab, CreativeModeTabs, Item}
import net.minecraft.world.level.block.{Block, Blocks}
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.event.server.ServerStartingEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.{FMLClientSetupEvent, FMLCommonSetupEvent}
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.{DeferredRegister, ForgeRegistries, RegistryObject}

import net.neoforged.srgutils.IMappingFile

import com.google.common.collect.ImmutableMap
import org.apache.logging.log4j.LogManager
import java.io.File
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

import net.silentchaos512.utils.Color
import net.silentchaos512.gear.api.traits.ITrait
import net.silentchaos512.gear.gear.`trait`.TraitManager
import net.silentchaos512.gear.api.stats.ItemStat
import net.silentchaos512.gear.api.stats.ItemStats
import net.silentchaos512.gear.api.stats.SplitItemStat
import net.silentchaos512.gear.api.item.GearType
import net.silentchaos512.gear.api.GearApi
import com.verdantartifice.primalmagick.PrimalMagick

import net.muridemo.soteriology.gear.`trait`.ForgeConditionalTrait
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableBlock
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.item.Rarity


/**
 * Converted from forge MDK in https://github.com/MinecraftForge/MinecraftForge
 */
@Mod(Soteriology.MOD_ID)
object Soteriology {
  final val MOD_ID = "soteriology"
  // Directly reference a log4j logger.
  final val LOGGER = LogManager.getLogger
  Soteriology.LOGGER.info("HELLO from Soteriology!")

  final val MANA_EFFICIENCY_ID = ResourceLocation(MOD_ID, "mana_efficiency")
  final val MANA_EFFICIENCY = ItemStats.register(SplitItemStat(Soteriology.MANA_EFFICIENCY_ID, 0f, 0f, 100f, Color.DODGERBLUE,
    ImmutableMap.of[GearType, java.lang.Float](
      GearType.HELMET, 4f,
      GearType.CHESTPLATE, 6f,
      GearType.LEGGINGS, 6f,
      GearType.BOOTS, 4f
    ),
    ItemStat.Properties().affectedByGrades(true).synergyApplies()
  ))

  final val WARDABLE_ID = ResourceLocation(PrimalMagick.MODID, "well_of_magick")
  final val HARMONIC_ID = ResourceLocation(MOD_ID, "harmonic")

  final val FORGE_CONDITIONAL_TRAIT = ResourceLocation(MOD_ID, "forge_conditional")
  GearApi.registerTraitSerializer(ForgeConditionalTrait.SERIALIZER)

  final val registerTrait: ITrait => ITrait =
    val lookup = MethodHandles.lookup()
    val method = classOf[TraitManager].getDeclaredMethod("addTrait", classOf[ITrait])
    method.setAccessible(true)
    val mh = lookup.unreflect(method).asType(MethodType.methodType(Void.TYPE, classOf[ITrait]))
    t => {mh.invoke(t); t}

  final val MAPPING = IMappingFile.load(this.getClass().getClassLoader().getResourceAsStream("/entity.tsrg"))
  // println("Dumping classes:")
  // MAPPING_FILE.getClasses().forEach(println)
  // println("Dumping methods of net.minecraft.world.entity.Entity:")
  // MAPPING_FILE.getClass("net.minecraft.world.entity.Entity".replace('.', '/')).getMethods().forEach(println)

  val BLOCKS: DeferredRegister[Block] = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)
  val ITEMS: DeferredRegister[Item] = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)
  val BLOCK_ENTITIES: DeferredRegister[BlockEntityType[?]] = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID)
  val CREATIVE_MODE_TABS: DeferredRegister[CreativeModeTab] = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID)

  val SUPERIOR_REFORGING_TABLE = BLOCKS.register(
    "superior_reforging_table",
    () => ReforgingTableBlock(BlockBehaviour.Properties.of().requiresCorrectToolForDrops().strength(5f, 1200f), 7)
  )
  val SUPERIOR_REFORGING_TABLE_ITEM = ITEMS.register(
    "superior_reforging_table",
    () => new BlockItem(SUPERIOR_REFORGING_TABLE.get(), new Item.Properties().rarity(Rarity.EPIC))
  )

  final val SGEAR_GENERICS = 
    ("helmet" :: "chestplate" :: "leggings" :: "boots" :: Nil).map {
      name => RegistryObject.create(new ResourceLocation("silentgear", name), ForgeRegistries.ITEMS)
    }

  // val EXAMPLE_BLOCK: RegistryObject[Block] = BLOCKS.register("example_block", () => new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)))
  // val EXAMPLE_BLOCK_ITEM: RegistryObject[Item] = ITEMS.register("example_block", () => new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()))

  // val EXAMPLE_ITEM: RegistryObject[Item] = ITEMS.register("example_item", () => new Item(new Item.Properties().food(new FoodProperties.Builder()
  //   .alwaysEat().nutrition(1).saturationMod(2f).build())))

  // val EXAMPLE_TAB: RegistryObject[CreativeModeTab] = CREATIVE_MODE_TABS.register("example_tab", () => CreativeModeTab.builder()
  //   .withTabsBefore(CreativeModeTabs.COMBAT)
  //   .icon(() => EXAMPLE_ITEM.get().getDefaultInstance)
  //   .displayItems((parameters, output) => {
  //     output.accept(EXAMPLE_ITEM.get())
  //   }).build()
  // )

  {
    val modEventBus = FMLJavaModLoadingContext.get().getModEventBus

    // Register the commonSetup method for modloading
    modEventBus.addListener(this.commonSetup)

    // Register the Deferred Register to the mod event bus so blocks get registered
    BLOCKS.register(modEventBus)
    // Register the Deferred Register to the mod event bus so items get registered
    ITEMS.register(modEventBus)
    // Register the Deferred Register to the mod event bus so tabs get registered
    CREATIVE_MODE_TABS.register(modEventBus)

    // Register ourselves for server and other game events we are interested in
    MinecraftForge.EVENT_BUS.register(this)

    // Register the item to a creative tab// Register the item to a creative tab
    modEventBus.addListener(this.addCreative)

    // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us// Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
    ModLoadingContext.get.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
  }

  private def commonSetup(event: FMLCommonSetupEvent): Unit = {
    // Some common setup code
    LOGGER.info("HELLO FROM COMMON SETUP")
    if (Config.logDirtBlock)
      LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT))
    LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber)
    Config.items.foreach(item => LOGGER.info("ITEM >> {}", item.toString))
  }

  private def addCreative(event: BuildCreativeModeTabContentsEvent): Unit = {
    if (event.getTabKey == CreativeModeTabs.FUNCTIONAL_BLOCKS)
      event.accept(SUPERIOR_REFORGING_TABLE_ITEM)
  }

  // You can use SubscribeEvent and let the Event Bus discover methods to call
  @SubscribeEvent
  def onServerStarting(event: ServerStartingEvent): Unit = {
    // Do something when the server starts
    LOGGER.info("HELLO from server starting")
  }

  // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
  @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Array(Dist.CLIENT))
  object ClientModEvents {
    @SubscribeEvent
    def onClientSetup(event: FMLClientSetupEvent): Unit = {
      // Some client setup code
      LOGGER.info("HELLO FROM CLIENT SETUP")
      LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance.getUser.getName)
    }
  }
}

package net.muridemo.soteriology.mixin

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Redirect
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

import com.verdantartifice.primalmagick.common.crafting.WardingModuleApplicationRecipe
import com.verdantartifice.primalmagick.common.items.armor.WardingModuleItem
import net.silentchaos512.gear.api.traits.ITrait
import net.silentchaos512.gear.util.GearHelper
import net.silentchaos512.gear.util.TraitHelper
// import net.silentchaos512.gear.setup.SgItems

import net.muridemo.soteriology.Soteriology
import net.muridemo.soteriology.gear.util.GearHelpers
import net.muridemo.soteriology.gear.util.GearHelpers.isWardable
import java.util.function.Supplier

@Mixin(Array(classOf[WardingModuleApplicationRecipe]))
abstract class WardingModuleRecipeMixin {
  @Redirect(
    method = Array("matches", "assemble"), 
    at = new At(value = "INVOKE", target = "m_204117_"),//"Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"),
    // remap = false,
    expect = 2
  )
  private def onStackIs(stack: ItemStack, tag: TagKey[Item]): Boolean = stack.isWardable
}
@Mixin(Array(classOf[WardingModuleItem]))
object WardingModuleItemMixin {
  @Redirect(
    method = Array("hasWardAttached"),
    at = new At(value = "INVOKE", target = "m_204117_"),
    expect = 1
  )
  private def onStackIs(stack: ItemStack, tag: TagKey[Item]): Boolean = stack.isWardable
  // @Inject(
  //   method = Array("getApplicableItems"),
  //   at = Array(new At(value = "RETURN")),
  //   cancellable = true,
  //   expect = 1
  // )
  // private def onGetApplicableItems[T <: Item](ci: CallbackInfoReturnable[List[Supplier[T]]]) = {
  //   val items = 
  //     ci.getReturnValue.appendedAll(
  //       (SgItems.HELMET :: SgItems.CHESTPLATE :: SgItems.LEGGINGS :: SgItems.BOOTS :: Nil).map(() => _))
  //   ci.setReturnValue(items)
  // }
}

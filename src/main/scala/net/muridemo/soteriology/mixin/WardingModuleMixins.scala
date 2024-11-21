package net.muridemo.soteriology.mixin

import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.silentchaos512.gear.api.traits.ITrait
import org.spongepowered.asm.mixin.Mixin
import com.verdantartifice.primalmagick.common.items.armor.WardingModuleItem
import com.verdantartifice.primalmagick.common.crafting.WardingModuleApplicationRecipe
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import org.spongepowered.asm.mixin.injection.Redirect
import net.minecraft.world.item.ItemStack
import net.silentchaos512.gear.util.GearHelper
import net.silentchaos512.gear.util.TraitHelper
import net.muridemo.soteriology.Soteriology

@Mixin(Array(classOf[WardingModuleApplicationRecipe]))//, classOf[WardingModuleItem]))
abstract class WardingModuleMixins {
  @Redirect(
    method = Array("matches"), 
    at = new At(value = "INVOKE", target = "m_204117_"),//"Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"),
    // remap = false,
    require = 1
  )
  private def onStackIs(stack: ItemStack, tag: TagKey[Item], callback: CallbackInfoReturnable[Boolean]): Unit = {
    println(s"onStackIs: stack=$stack, tag=$tag")
    if (GearHelper.isGear(stack)) 
      println(s"onStackIs: hasTrait=${TraitHelper.hasTrait(stack, Soteriology.WARDABLE_ID)}")
      callback.setReturnValue(TraitHelper.hasTrait(stack, Soteriology.WARDABLE_ID))
  }
}

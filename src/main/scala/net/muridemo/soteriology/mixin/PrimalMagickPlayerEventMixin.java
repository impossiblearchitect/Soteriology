package net.muridemo.soteriology.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.verdantartifice.primalmagick.common.attunements.AttunementManager;
import com.verdantartifice.primalmagick.common.attunements.AttunementThreshold;
import com.verdantartifice.primalmagick.common.effects.EffectsPM;
import com.verdantartifice.primalmagick.common.events.PlayerEvents;
import com.verdantartifice.primalmagick.common.items.armor.IManaDiscountGear;
import com.verdantartifice.primalmagick.common.items.armor.WardingModuleItem;
import com.verdantartifice.primalmagick.common.sources.Source;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

// import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
// import com.llamalad7.mixinextras.injector.wrapoperation.Operation
// import org.spongepowered.asm.mixin.injection.Inject
// import org.spongepowered.asm.mixin.injection.Redirect
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable

// import net.muridemo.soteriology.util.Helpers.*

// import net.muridemo.soteriology.gear.util.GearHelpers.*;

// import scala.compiletime.*
// import scala.compiletime.ops.any.*
// import scala.compiletime.ops.string.*

@Debug(export = true)
@Mixin(PlayerEvents.class)
public abstract class PrimalMagickPlayerEventMixin {
  // inline def mkTarget = ${ inlineStringInterp('{Seq("intValue = ")})('{WardingModuleItem.REGEN_COST.toString}) }
  // inline val TARGET = "intValue = " + constValue[ToString[WardingModuleItem.REGEN_COST.type]]
  @Unique
  private static double soteriology$passiveCostModifiers(Player player, Source source) {
    double gearDiscount = 0.0;
    for (ItemStack stack : player.getArmorSlots()) {
      if (stack.getItem() instanceof IManaDiscountGear gear) {
        gearDiscount += gear.getManaDiscount(stack, player, source);
      }
    }
    gearDiscount *= 0.01;
    double attunementDiscount = 
      AttunementManager.meetsThreshold(player, source, AttunementThreshold.MINOR) ? 0.05 : 0;
    MobEffectInstance manafruit = player.getEffect(EffectsPM.MANAFRUIT.get());
    MobEffectInstance impedance = player.getEffect(EffectsPM.MANA_IMPEDANCE.get());
    double effectDiscount = 
      manafruit != null ? 0.01 * (2 * manafruit.getAmplifier() + 1.0) : 0.0;
    double effectPenalty =
      impedance != null ? 0.05 * impedance.getAmplifier() + 1.0 : 0.0;
    return 1.0 - (gearDiscount + attunementDiscount + effectDiscount - effectPenalty);
  }

//  @Definition(id = "REGEN_COST", field = WardingModuleItem.REGEN_COST)
//  @Expression("REGEN_COST")
  @ModifyExpressionValue(
          method = "handleWardRegeneration",
//          at = @At("MIXINEXTRAS:EXPRESSION")
          at = @At(value = "CONSTANT", args = "intValue=500"))// + WardingModuleItem.REGEN_COST))
  private static int adjustWardRegenCost(int original, ServerPlayer player) {
    return (int) Math.round(original * soteriology$passiveCostModifiers((Player) player, Source.EARTH));
  }


//  @ModifyExpressionValue(
//    method = "handleWardRegeneration",
//    at = @At(value = "CONSTANT", args = "intValue=500"),
//    require = 1)
//  private static int adjustWardRegenCost(int original, ServerPlayer player) {
//    return (int) Math.round(original * soteriology$passiveCostModifiers(player, Source.EARTH));
//  }
}

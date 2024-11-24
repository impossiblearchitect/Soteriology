package net.muridemo.soteriology.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.verdantartifice.primalmagick.client.events.ClientRenderEvents;
import com.verdantartifice.primalmagick.common.items.armor.IManaDiscountGear;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(ClientRenderEvents.class)
public abstract class PrimalMagickClientRenderEventMixin {
    @Definition(id = "IManaDiscountGear", type = IManaDiscountGear.class)
    @Expression("? instanceof IManaDiscountGear")
    @WrapOperation(method = "renderTooltip", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean renderTooltipCheck(Object object, Operation<Boolean> original, ItemTooltipEvent event) {
        return original.call(object) && ((IManaDiscountGear) object).getBestManaDiscount(event.getItemStack(), event.getEntity()) > 0;
    }
}

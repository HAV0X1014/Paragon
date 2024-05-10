package com.paragon.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.impl.event.render.gui.RenderTooltipEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public class MixinGuiScreen {

    @Inject(method = "renderToolTip", at = @At("HEAD"), cancellable = true)
    public void hookRenderToolTip(ItemStack stack, int x, int y, CallbackInfo ci) {
        RenderTooltipEvent event = new RenderTooltipEvent(stack, x, y);
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}

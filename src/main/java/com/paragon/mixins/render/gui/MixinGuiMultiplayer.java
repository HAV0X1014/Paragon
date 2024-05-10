package com.paragon.mixins.render.gui;

import com.paragon.impl.ui.alt.AltManagerGUI;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMultiplayer.class)
public class MixinGuiMultiplayer extends GuiScreen {

    @Inject(method = "initGui", at = @At("TAIL"))
    public void hookInitGui(CallbackInfo ci) {
        this.buttonList.add(new GuiButton(- 3245, 5, height - 25, 75, 20, "Alts"));
    }

    @Inject(method = "actionPerformed", at = @At("TAIL"))
    public void hookActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == - 3245) {
            mc.displayGuiScreen(new AltManagerGUI());
        }
    }

}

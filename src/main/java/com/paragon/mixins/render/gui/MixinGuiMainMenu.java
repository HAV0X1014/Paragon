package com.paragon.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.impl.module.client.Colours;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiMainMenu.class, priority = Integer.MAX_VALUE)
public class MixinGuiMainMenu extends GuiScreen {

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void hookDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow("Paragon " + TextFormatting.GRAY + Paragon.VERSION, 2, 2, Colours.mainColour.getValue().getRGB());
    }

}

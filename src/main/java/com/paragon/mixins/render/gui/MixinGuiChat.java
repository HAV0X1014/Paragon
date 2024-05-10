package com.paragon.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.impl.event.render.gui.RenderChatGuiEvent;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiChat.class, priority = Integer.MAX_VALUE)
public abstract class MixinGuiChat extends GuiScreen {

    @Shadow
    protected GuiTextField inputField;

    @Inject(method = "drawScreen", at = @At("HEAD"))
    public void hookDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (Keyboard.isKeyDown(Keyboard.KEY_UP) && !Paragon.INSTANCE.getCommandManager().getLastCommand().isEmpty()) {
            this.inputField.setText(Paragon.INSTANCE.getCommandManager().getLastCommand());
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiTextField;drawTextBox()V", shift = At.Shift.BEFORE))
    public void hookDrawScreenTail(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        RenderChatGuiEvent event = new RenderChatGuiEvent(inputField.getText());
        Paragon.INSTANCE.getEventBus().post(event);

        inputField.setText(event.getText());
    }

    @Inject(method = "keyTyped", at = @At("HEAD"), cancellable = true)
    public void hookKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        // prevent weird tab thing??
        if (inputField.getText().startsWith(Paragon.INSTANCE.getCommandManager().getPrefix()) && keyCode == Keyboard.KEY_TAB) {
            ci.cancel();
        }
    }

}

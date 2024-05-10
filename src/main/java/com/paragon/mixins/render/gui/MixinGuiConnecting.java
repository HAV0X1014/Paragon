package com.paragon.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.impl.event.network.ServerEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public class MixinGuiConnecting extends GuiScreen {

    @Inject(method = "connect", at = @At("HEAD"))
    private void onPreConnect(CallbackInfo info) {
        Paragon.INSTANCE.getEventBus().post(new ServerEvent.Connect(ServerEvent.Connect.State.PRE));
    }

}
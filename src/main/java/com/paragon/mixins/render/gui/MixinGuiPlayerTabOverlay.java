package com.paragon.mixins.render.gui;

import com.paragon.Paragon;
import com.paragon.impl.event.render.gui.TabListEvent;
import com.paragon.impl.event.render.gui.TabOverlayEvent;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Ljava/util/List;subList(II)Ljava/util/List;", remap = false))
    public List<NetworkPlayerInfo> hookRenderPlayerList(List<NetworkPlayerInfo> list, int i, int j) {
        TabListEvent event = new TabListEvent(list.size());
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            return list.subList(0, (int) MathHelper.clamp(list.size(), 0, event.getSize()));
        } else {
            return list.subList(0, Math.min(list.size(), 80));
        }
    }

    @Inject(method = "getPlayerName", at = @At("HEAD"), cancellable = true)
    public void hookGetPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable<String> cir) {
        TabOverlayEvent event = new TabOverlayEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            cir.cancel();
            cir.setReturnValue(networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName()));
        }
    }

}

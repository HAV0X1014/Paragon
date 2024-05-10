package com.paragon.mixins;

import com.paragon.Paragon;
import com.paragon.impl.event.player.ClickBothMouseButtonsEvent;
import com.paragon.impl.event.render.GetFramerateLimitEvent;
import com.paragon.impl.event.render.gui.GuiUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Shadow
    public WorldClient world;

    @Shadow
    public GameSettings gameSettings;

    @Shadow
    public GuiScreen currentScreen;

    @Shadow public PlayerControllerMP playerController;

    @Shadow public EntityPlayerSP player;

    @Inject(method = "displayGuiScreen", at = @At("HEAD"), cancellable = true)
    public void hookDisplayGuiScreen(GuiScreen guiScreenIn, CallbackInfo ci) {
        GuiUpdateEvent event = new GuiUpdateEvent(guiScreenIn);
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * @author Surge
     * @reason GUIs can look laggy when not ingame without this
     */
    @Overwrite
    public int getLimitFramerate() {
        GetFramerateLimitEvent event = new GetFramerateLimitEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            return event.getLimit();
        }

        return world == null && this.currentScreen != null ? MathHelper.clamp(this.gameSettings.limitFramerate, 0, 240) : this.gameSettings.limitFramerate;
    }

    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    public boolean hookSendClickBlockToController(EntityPlayerSP instance) {
        ClickBothMouseButtonsEvent event = new ClickBothMouseButtonsEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        return !event.isCancelled() && this.player.isHandActive();
    }

    @Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z"))
    public boolean hookRightClickMouse(PlayerControllerMP instance) {
        ClickBothMouseButtonsEvent event = new ClickBothMouseButtonsEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        return !event.isCancelled() && this.playerController.getIsHittingBlock();
    }

}

package com.paragon.mixins.input;

import com.paragon.Paragon;
import com.paragon.impl.event.input.KeybindingPressedEvent;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {

    @Shadow
    private boolean pressed;
    @Shadow
    private int keyCode;

    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    public void hookIsKeyDown(CallbackInfoReturnable<Boolean> info) {
        KeybindingPressedEvent event = new KeybindingPressedEvent(keyCode, pressed);
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            info.setReturnValue(event.getPressedState());
        }
    }
}

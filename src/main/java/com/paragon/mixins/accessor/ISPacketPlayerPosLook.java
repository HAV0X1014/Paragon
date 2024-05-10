package com.paragon.mixins.accessor;

import net.minecraft.network.play.server.SPacketPlayerPosLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SPacketPlayerPosLook.class)
public interface ISPacketPlayerPosLook {

    @Accessor("yaw")
    void hookSetYaw(float newYaw);

    @Accessor("pitch")
    void hookSetPitch(float newPitch);

}

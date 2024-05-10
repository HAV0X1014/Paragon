package com.paragon.mixins.accessor;

import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CPacketPlayer.class)
public interface ICPacketPlayer {

    @Accessor("yaw")
    float hookGetYaw();

    @Accessor("yaw")
    void hookSetYaw(float yaw);

    @Accessor("pitch")
    float hookGetPitch();

    @Accessor("pitch")
    void hookSetPitch(float pitch);

    @Accessor("y")
    void hookSetY(double y);

    @Accessor("onGround")
    void hookSetOnGround(boolean onGround);

    @Accessor("moving")
    boolean hookIsMoving();

}

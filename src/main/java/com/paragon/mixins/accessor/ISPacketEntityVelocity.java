package com.paragon.mixins.accessor;

import net.minecraft.network.play.server.SPacketEntityVelocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Surge
 */
@Mixin(SPacketEntityVelocity.class)
public interface ISPacketEntityVelocity {

    @Accessor("motionX")
    void hookSetMotionX(int newMotionX);

    @Accessor("motionY")
    void hookSetMotionY(int newMotionY);

    @Accessor("motionZ")
    void hookSetMotionZ(int newMotionZ);

}

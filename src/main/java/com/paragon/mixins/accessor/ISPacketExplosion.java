package com.paragon.mixins.accessor;

import net.minecraft.network.play.server.SPacketExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Surge
 */
@Mixin(SPacketExplosion.class)
public interface ISPacketExplosion {

    @Accessor("motionX")
    void hookSetMotionX(float newMotionX);

    @Accessor("motionY")
    void hookSetMotionY(float newMotionY);

    @Accessor("motionZ")
    void hookSetMotionZ(float newMotionZ);

}

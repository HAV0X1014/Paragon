package com.paragon.mixins.accessor;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Surge
 * @since 29/11/2022
 */
@Mixin(CPacketCustomPayload.class)
public interface ICPacketCustomPayload {

    @Accessor("data")
    void hookSetData(PacketBuffer buffer);

}

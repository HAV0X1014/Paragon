package com.paragon.mixins.accessor;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

/**
 * @author Surge
 * @since 26/11/2022
 */
@Mixin(NetworkManager.class)
public interface INetworkManager {

    @Invoker("dispatchPacket")
    void hookDispatchPacket(final Packet<?> inPacket, @Nullable final GenericFutureListener<? extends Future<? super Void >>[] futureListeners);

}

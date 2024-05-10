package com.paragon.mixins.network;

import com.paragon.Paragon;
import com.paragon.impl.event.network.PacketEvent;
import com.paragon.impl.event.network.ServerEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    public void headHookChannelRead0(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci) {
        PacketEvent.PreReceive preReceive = new PacketEvent.PreReceive(p_channelRead0_2_);
        Paragon.INSTANCE.getEventBus().post(preReceive);

        if (preReceive.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPacket*", at = @At("HEAD"), cancellable = true)
    public void headHookSendPacket(Packet<?> packetIn, CallbackInfo ci) {
        PacketEvent.PreSend preSend = new PacketEvent.PreSend(packetIn);
        Paragon.INSTANCE.getEventBus().post(preSend);

        if (preSend.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0*", at = @At("TAIL"))
    public void tailHookChannelRead0(ChannelHandlerContext ctx, Packet<?> packet, CallbackInfo ci) {
        PacketEvent.PostReceive postReceive = new PacketEvent.PostReceive(packet);
        Paragon.INSTANCE.getEventBus().post(postReceive);
    }

    @Inject(method = "sendPacket*", at = @At("TAIL"))
    public void tailHookSendPacket(Packet<?> packetIn, CallbackInfo ci) {
        PacketEvent.PostSend postSend = new PacketEvent.PostSend(packetIn);
        Paragon.INSTANCE.getEventBus().post(postSend);
    }

    @Inject(method = "closeChannel", at = @At(value = "HEAD"))
    private void onCloseChannel(CallbackInfo ci) {
        Paragon.INSTANCE.getEventBus().post(new ServerEvent.Disconnect(true));
    }
}

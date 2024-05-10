package com.paragon.mixins.player;

import com.paragon.Paragon;
import com.paragon.impl.event.player.PlayerMoveEvent;
import com.paragon.impl.event.player.UpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * credit - cosmos
 */
@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    @Shadow
    private float lastReportedYaw;

    @Shadow
    private float lastReportedPitch;

    @Shadow
    private double lastReportedPosX;

    @Shadow
    private double lastReportedPosY;

    @Shadow
    private double lastReportedPosZ;

    @Shadow
    protected Minecraft mc;

    @Shadow
    private boolean prevOnGround;

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Shadow
    private boolean autoJumpEnabled;

    @Shadow
    public abstract void move(MoverType type, double x, double y, double z);

    public MixinEntityPlayerSP() {
        super(null, null);
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void hookOnUpdateWalkingPlayer(CallbackInfo ci) {
        UpdateEvent updateEvent = new UpdateEvent();
        Paragon.INSTANCE.getEventBus().post(updateEvent);

        if (updateEvent.isCancelled()) {
            ci.cancel();

            if (isCurrentViewEntity()) {
                boolean motUpdate = updateEvent.getX() != lastReportedPosX || updateEvent.getY() != lastReportedPosY || updateEvent.getZ() != lastReportedPosZ;
                boolean rotUpdate = updateEvent.getYaw() - lastReportedYaw != 0 || updateEvent.getPitch() - lastReportedPitch != 0;

                if (motUpdate && rotUpdate) {
                    mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(updateEvent.getX(), updateEvent.getY(), updateEvent.getZ(), updateEvent.getYaw(), updateEvent.getPitch(), updateEvent.isOnGround()));
                } else if (motUpdate) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(updateEvent.getX(), updateEvent.getY(), updateEvent.getZ(), updateEvent.isOnGround()));
                } else if (rotUpdate) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Rotation(updateEvent.getYaw(), updateEvent.getPitch(), updateEvent.isOnGround()));
                } else if (prevOnGround != updateEvent.isOnGround()) {
                    mc.player.connection.sendPacket(new CPacketPlayer(updateEvent.isOnGround()));
                }

                if (motUpdate) {
                    lastReportedPosX = updateEvent.getX();
                    lastReportedPosY = updateEvent.getY();
                    lastReportedPosZ = updateEvent.getZ();
                }

                if (rotUpdate) {
                    lastReportedYaw = updateEvent.getYaw();
                    lastReportedPitch = updateEvent.getPitch();
                }

                prevOnGround = updateEvent.isOnGround();
                autoJumpEnabled = mc.gameSettings.autoJump;
            }
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void hookMove(AbstractClientPlayer instance, MoverType moverType, double x, double y, double z) {
        PlayerMoveEvent moveEvent = new PlayerMoveEvent(x, y, z);
        Paragon.INSTANCE.getEventBus().post(moveEvent);

        super.move(moverType, moveEvent.getX(), moveEvent.getY(), moveEvent.getZ());
    }

}
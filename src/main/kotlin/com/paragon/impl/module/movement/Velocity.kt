package com.paragon.impl.module.movement

import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.event.world.entity.EntityPushEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.ISPacketEntityVelocity
import com.paragon.mixins.accessor.ISPacketExplosion
import com.paragon.util.mc
import net.minecraft.network.play.server.SPacketEntityVelocity
import net.minecraft.network.play.server.SPacketExplosion

/**
 * @author Surge
 */
@Aliases(["AntiKnockback"])
object Velocity : Module("Velocity", Category.MOVEMENT, "Stops crystals and mobs from causing you knockback") {

    private val velocityPacket = Setting("VelocityPacket", true) describedBy "Cancels or modifies the velocity packet"
    private val explosions = Setting("Explosions", true) describedBy "Cancels or modifies the explosion knockback"
    private val horizontal = Setting("Horizontal", 0f, 0f, 100f, 1f) describedBy "The horizontal modifier"
    private val vertical = Setting("Vertical", 0f, 0f, 100f, 1f) describedBy "The vertical modifier"
    private val noPush = Setting("NoPush", true) describedBy "Prevents the player from being pushed by entities"

    @Listener
    fun onPacket(event: PreReceive) {
        if (event.packet is SPacketEntityVelocity && velocityPacket.value) {
            // Check it is for us
            if (event.packet.entityID == mc.player.entityId) {
                // We can just cancel the packet if both horizontal and vertical are 0
                if (horizontal.value == 0f && vertical.value == 0f) {
                    event.cancel()
                }
                else {
                    (event.packet as ISPacketEntityVelocity).hookSetMotionX(((event.packet as SPacketEntityVelocity).motionX / 100 * (horizontal.value / 100)).toInt())
                    (event.packet as ISPacketEntityVelocity).hookSetMotionY(vertical.value.toInt() / 100)
                    (event.packet as ISPacketEntityVelocity).hookSetMotionZ(((event.packet as SPacketEntityVelocity).motionZ / 100 * (horizontal.value / 100)).toInt())
                }
            }
        }
        if (event.packet is SPacketExplosion && explosions.value) {
            // We can just cancel the packet if both horizontal and vertical are 0
            if (horizontal.value == 0f && vertical.value == 0f) {
                event.cancel()
            }
            else {
                (event.packet as ISPacketExplosion).hookSetMotionX(horizontal.value / 100 * (event.packet as SPacketExplosion).motionX)
                (event.packet as ISPacketExplosion).hookSetMotionY(vertical.value / 100 * (event.packet as SPacketExplosion).motionY)
                (event.packet as ISPacketExplosion).hookSetMotionZ(horizontal.value / 100 * (event.packet as SPacketExplosion).motionZ)
            }
        }
    }

    @Listener
    fun onEntityPush(event: EntityPushEvent) {
        if (noPush.value && event.entity === mc.player) {
            event.cancel()
        }
    }

    override fun getData(): String {
        return "H% " + horizontal.value + ", V% " + vertical.value
    }

}
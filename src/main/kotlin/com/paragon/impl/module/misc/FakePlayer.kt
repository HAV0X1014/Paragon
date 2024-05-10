package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.impl.event.combat.TotemPopEvent
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.combat.CrystalUtil
import com.paragon.util.mc
import com.paragon.util.player.EntityFakePlayer
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.network.play.server.SPacketDestroyEntities

/**
 * @author Surge
 */
object FakePlayer : Module("FakePlayer", Category.MISC, "Spawns a fake client side player") {

    private val popAble = Setting("Pop", true) describedBy "Only works for PopChams"

    private var fakePlayer: EntityFakePlayer? = null

    override fun onEnable() {
        if (mc.anyNull) {
            return
        }

        // Create new fake player
        fakePlayer = EntityFakePlayer(mc.world)
    }

    override fun onDisable() {
        // If we can despawn the player, do so
        if (fakePlayer != null && mc.world != null && mc.world.loadedEntityList.contains(fakePlayer)) {
            (fakePlayer ?: return).despawn()
        }
    }

    @Listener
    fun onPacket(event: PacketEvent.PreReceive) {
        if (event.packet is SPacketDestroyEntities && popAble.value) {
            event.packet.entityIDs.forEach {
                val entity = mc.world.getEntityByID(it)

                if (entity !is EntityEnderCrystal) {
                    return@forEach
                }

                if (fakePlayer == null) {
                    return
                }

                val damage = CrystalUtil.calculateDamage(entity.positionVector, fakePlayer ?: return@forEach)

                if (damage > 0) {
                    if ((fakePlayer ?: return@forEach).health - damage > 0) {
                        (fakePlayer ?: return@forEach).health -= damage
                    }
                    else {
                        Paragon.INSTANCE.eventBus.post(TotemPopEvent(fakePlayer ?: return@forEach))
                        (fakePlayer ?: return@forEach).health = (fakePlayer ?: return@forEach).maxHealth
                    }
                }
            }
        }
    }

}
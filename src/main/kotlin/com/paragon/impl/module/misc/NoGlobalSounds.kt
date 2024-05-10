package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import net.minecraft.init.SoundEvents
import net.minecraft.network.play.server.SPacketEffect
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.util.SoundCategory

/**
 * @author Surge
 * @since 14/05/22
 */
object NoGlobalSounds : Module("NoGlobalSounds", Category.MISC, "Prevents global sounds from playing") {

    // Sounds to cancel
    private val endPortal = Setting(
        "EndPortal", true
    ) describedBy "Disables the end portal spawn sound"

    private val witherSpawn = Setting(
        "WitherSpawn", true
    ) describedBy "Disables the wither spawn sound"

    private val dragonDeath = Setting(
        "DragonDeath", true
    ) describedBy "Disables the dragon death sound"

    private val lightning = Setting(
        "Lightning", true
    ) describedBy "Disables the lightning sound"

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is SPacketSoundEffect) {
            val packet = event.packet

            if (packet.category == SoundCategory.WEATHER && packet.sound == SoundEvents.ENTITY_LIGHTNING_THUNDER && lightning.value) {
                event.cancel()
            }
        }

        if (event.packet is SPacketEffect) {
            val packet = event.packet

            if (packet.soundType == 1038 && endPortal.value || packet.soundType == 1023 && witherSpawn.value || packet.soundType == 1028 && dragonDeath.value) {
                event.cancel()
            }
        }
    }

}
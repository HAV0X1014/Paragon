package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author Surge
 */
object OnDeath : Module("OnDeath", Category.MISC, "Do certain actions when you die") {

    private val printCoords = Setting(
        "PrintCoords", true
    ) describedBy "Prints your death coordinates in chat (client-side only)"

    private val respawn = Setting(
        "Respawn", true
    ) describedBy "Respawns you after death"

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        // Check that the entity that died has the same ID that the player does
        if (mc.anyNull || event.entity.entityId != mc.player.entityId) {
            return
        }

        val entity = event.entity
        if (printCoords.value) {
            val pos = entity.position

            // Display the client message
            Paragon.INSTANCE.commandManager.sendClientMessage("${TextFormatting.RED}You died at ${TextFormatting.WHITE}X${TextFormatting.GRAY} ${pos.x} ${TextFormatting.WHITE}Y${TextFormatting.GRAY} ${pos.y} ${TextFormatting.WHITE}Z${TextFormatting.GRAY} ${pos.z}")
        }

        if (respawn.value) {
            // Respawn the player
            mc.player.respawnPlayer()
        }
    }

}
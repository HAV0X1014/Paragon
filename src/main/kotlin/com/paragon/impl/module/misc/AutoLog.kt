package com.paragon.impl.module.misc

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.client.gui.GuiMainMenu
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

/**
 * @author Surge
 */
@SideOnly(Side.CLIENT)
object AutoLog : Module("AutoLog", Category.MISC, "Automatically logs you out when you reach a certain health") {

    private val logMode = Setting(
        "LogMode", DisconnectMode.DISCONNECT
    ) describedBy "How to log you out of the server"

    private val health = Setting(
        "Health", 6f, 1f, 20f, 1f
    ) describedBy "The health to log you out at"

    private val autoDisable = Setting(
        "AutoDisable", true
    ) describedBy "Disables the module after logging you out"

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        if (mc.player.health <= health.value) {
            when (logMode.value) {
                DisconnectMode.KICK -> mc.player.inventory.currentItem = -1

                DisconnectMode.DISCONNECT -> {
                    // Disconnect from server
                    mc.world.sendQuittingDisconnectingPacket()
                    mc.loadWorld(null)
                    mc.displayGuiScreen(GuiMainMenu())
                }
            }

            if (autoDisable.value) {
                // Toggle module state
                toggle()
            }
        }
    }

    enum class DisconnectMode {
        /**
         * Disconnects you from the server
         */
        DISCONNECT,

        /**
         * Kicks you from the server by setting your current item to an invalid slot
         */
        KICK
    }

}
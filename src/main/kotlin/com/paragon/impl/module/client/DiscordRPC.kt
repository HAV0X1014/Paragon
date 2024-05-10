package com.paragon.impl.module.client

import com.paragon.Paragon
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.impl.module.annotation.IgnoredByNotifications
import com.paragon.impl.module.annotation.NotVisibleByDefault

/**
 * @author Surge
 */
@IgnoredByNotifications
@NotVisibleByDefault
object DiscordRPC : Module("DiscordRPC", Category.CLIENT, "Changes your Discord presence to reflect the client's current state") {

    val showServer = Setting(
        "Show Server", true
    ) describedBy "Show the servers ip in the RPC"

    override fun onEnable() {
        Paragon.INSTANCE.presenceManager.startRPC()
    }

    override fun onTick() {
        Paragon.INSTANCE.presenceManager.updateRPC()
    }

    override fun onDisable() {
        Paragon.INSTANCE.presenceManager.stopRPC()
    }

}
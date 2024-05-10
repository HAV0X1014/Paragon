package com.paragon.impl.managers

import com.paragon.impl.managers.notifications.Notification
import net.minecraftforge.common.MinecraftForge
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Surge
 */
class NotificationManager {

    val notifications = CopyOnWriteArrayList<Notification>()

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun addNotification(notification: Notification) = notifications.add(notification).let { return@let }

}
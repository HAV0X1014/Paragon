package com.paragon.impl.event

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.event.network.PlayerEvent.PlayerJoinEvent
import com.paragon.impl.event.network.PlayerEvent.PlayerLeaveEvent
import com.paragon.impl.event.render.gui.RenderChatGuiEvent
import com.paragon.impl.module.client.Notifications
import com.paragon.impl.module.hud.EditorGUI
import com.paragon.impl.module.hud.HUDModule
import com.paragon.util.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.SPacketPlayerListItem
import net.minecraft.network.play.server.SPacketPlayerListItem.AddPlayerData
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class EventFactory {

    init {
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.eventBus.register(this)
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        Paragon.INSTANCE.moduleManager.modules.forEach {
            if (it.isEnabled) {
                it.onTick()
            }

            if (it.bind.value.isPressed() && Minecraft.getMinecraft().currentScreen == null) {
                Paragon.INSTANCE.eventBus.unregister(it)
                it.toggle()
            }
        }
    }

    @SubscribeEvent
    fun onRender2D(event: RenderGameOverlayEvent.Post) {
        if (event.type.equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            Paragon.INSTANCE.moduleManager.modules.forEach {
                if (it.isEnabled) {
                    it.onRender2D()

                    if (it is HUDModule && mc.currentScreen !is EditorGUI) {
                        it.draw()
                    }
                }
            }

            if (Notifications.isEnabled) {
                when (Notifications.display.value) {
                    Notifications.Display.POP_OUT -> {
                        val resolution = ScaledResolution(mc)
                        var notificationY = resolution.scaledHeight - 25f

                        Paragon.INSTANCE.notificationManager.notifications.removeIf { it.animation.getAnimationFactor() == 0.0 && it.progress.getAnimationFactor() == 1.0 }

                        Paragon.INSTANCE.notificationManager.notifications.forEach {
                            it.render(notificationY)

                            notificationY -= 25 * it.animation.getAnimationFactor().toFloat()
                        }
                    }

                    Notifications.Display.CHAT -> {
                        Paragon.INSTANCE.notificationManager.notifications.forEach {
                            Paragon.INSTANCE.commandManager.sendClientMessage(it.message)
                        }

                        Paragon.INSTANCE.notificationManager.notifications.clear()
                    }
                }
            }

            // kek
            // just to prevent funny rendering issues
            glEnable(GL_BLEND)
            glEnable(GL_DEPTH_TEST)
        }
    }

    @SubscribeEvent
    fun onRender3D(event: RenderWorldLastEvent?) {
        Paragon.INSTANCE.moduleManager.modules.forEach {
            if (it.isEnabled) {
                it.onRender3D()
            }
        }
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is SPacketPlayerListItem) {
            val packet = event.packet

            when (packet.action) {
                SPacketPlayerListItem.Action.ADD_PLAYER -> packet.entries.forEach { entry: AddPlayerData ->
                    if (entry.profile.name != null) {
                        Paragon.INSTANCE.eventBus.post(
                            PlayerJoinEvent(entry.profile.name)
                        )
                    }
                }

                SPacketPlayerListItem.Action.REMOVE_PLAYER -> packet.entries.forEach { entry: AddPlayerData ->
                    if (entry.profile.name != null) {
                        Paragon.INSTANCE.eventBus.post(
                            PlayerLeaveEvent(entry.profile.name)
                        )
                    }
                }

                else -> {}
            }
        }
    }

    @Listener
    fun onRenderChatGui(event: RenderChatGuiEvent) {
        if (event.text.startsWith(Paragon.INSTANCE.commandManager.prefix)) {
            val resolution = ScaledResolution(mc)

            mc.fontRenderer.drawStringWithShadow("[TAB] to automatically fill in the next argument", 5f, resolution.scaledHeight - 25f, Color.GRAY.rgb)

            val full = event.text.substring(1, event.text.length).split(" ").toMutableList()

            // no command
            if (full[0].isEmpty()) {
                return
            }

            // get command
            val command = Paragon.INSTANCE.commandManager.commands.firstOrNull { it.name.startsWith(full[0], true) } ?: return

            if (event.text.substring(1, event.text.length).contains(" ") && !Paragon.INSTANCE.commandManager.commands.any { it.name.equals(full[0], ignoreCase = true) }) {
                return
            }

            val givenArguments = arrayListOf<String>()

            // oh my
            full.forEachIndexed { index, part ->
                if (index > 0) {
                    givenArguments.add(part)
                }
            }

            // get correct syntax

            var nonTypedSyntax = ""
            var markInvalid = false

            // command name
            if (!nonTypedSyntax.contains(command.name, true)) {
                nonTypedSyntax += command.name.replaceFirst(full[0], "", true)
            }

            command.syntax.arguments.forEachIndexed { index, argument ->
                // LMAOOOOOO WHAT THE FUCK
                // Throwback to https://github.com/momentumdevelopment/cosmos/pull/171/commits/816b5636b68378226f3570ce8fc6ae7946bdaa0f
                runCatching {
                    if (!argument.isVisible(givenArguments)) {
                        return@forEachIndexed
                    }

                    val given = givenArguments[index]

                    if (given.isEmpty()) {
                        nonTypedSyntax += "<${argument.valid.joinToString("|", transform = { valid -> valid })}>".replace("any_str", argument.name)
                    } else if (!argument.isComplete(givenArguments[index])) {
                        val firstValid = argument.getFirstValidOption(givenArguments[index]).replace(givenArguments[index], "", true)

                        nonTypedSyntax += firstValid.ifEmpty {
                            markInvalid = true
                            ""
                        }
                    }
                }.onFailure {
                    nonTypedSyntax += " <${argument.valid.joinToString("|", transform = { valid -> valid } )}>".replace("any_str", argument.name)
                }
            }

            // draw behind text
            mc.fontRenderer.drawStringWithShadow(
                (if (markInvalid) "${TextFormatting.RED}" else "") + nonTypedSyntax,
                4f + mc.fontRenderer.getStringWidth(event.text),
                resolution.scaledHeight - 12f,
                Color.GRAY.rgb
            )

            val nextArgument = nonTypedSyntax.split(" ")

            if (nextArgument.isNotEmpty()) {
                val next = nextArgument[0]

                // is preview
                if (next.contains("<")) {
                    return
                }

                val repeat = Keyboard.areRepeatEventsEnabled()
                Keyboard.enableRepeatEvents(false)

                if (Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
                    event.text += next
                }

                Keyboard.enableRepeatEvents(repeat)
            }
        }
    }

}
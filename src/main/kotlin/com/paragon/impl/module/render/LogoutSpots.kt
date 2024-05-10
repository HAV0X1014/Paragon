package com.paragon.impl.module.render

import com.paragon.Paragon
import com.paragon.impl.event.network.PlayerEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.managers.notifications.Notification
import com.paragon.impl.managers.notifications.NotificationType
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IEntityRenderer
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.render.shader.shaders.OutlineShader
import com.paragon.util.toBinary
import com.paragon.util.world.BlockUtil
import io.ktor.util.collections.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor

/**
 * @author Surge
 * @since 06/09/2022
 */
object LogoutSpots : Module("LogoutSpots", Category.RENDER, "Shows where players have logged out") {

    private val renderMode = Setting("RenderMode", RenderMode.SHADER) describedBy "How to render the logout spots"

    private val outline = Setting("Outline", true) describedBy "Outline the fill" subOf renderMode visibleWhen { renderMode.value == RenderMode.SHADER }
    private val fill = Setting("Fill", true) describedBy "Fill the outline" subOf renderMode visibleWhen { renderMode.value == RenderMode.SHADER }

    private val width = Setting("Width", 0.5f, 0.1f, 2f, 0.1f) describedBy "The width of the lines"

    private val range = Setting("Range", 64.0, 16.0, 256.0, 1.0) describedBy "The range to check for players"

    private val notify = Setting("Notify", false) describedBy "Notify you when a player logs out or logs back in"
    private val logIn = Setting("LogIn", true) describedBy "Notify you when a player logs in" subOf notify
    private val logInType = Setting("LogInType", NotificationType.WARNING) describedBy "The type of notification" subOf notify visibleWhen { logIn.value }
    private val logOut = Setting("LogOut", false) describedBy "Notify you when a player logs out" subOf notify
    private val logOutType = Setting("LogOutType", NotificationType.INFO) describedBy "The type of notification" subOf notify visibleWhen { logOut.value }

    private val box = Setting("Box", BoxRenderMode.BOTH) describedBy "Render the placement" visibleWhen { renderMode.value == RenderMode.BOX }

    private val renderNametag = Setting("Nametag", true) describedBy "Render the nametag" subOf box
    private val nametagYOffset = Setting("NametagYOffset", 1.1, 0.0, 3.0, 0.1) subOf box visibleWhen { renderNametag.value }

    private val timeNametag = Setting("TimeNametag", true) describedBy "Render the time nametag" subOf box
    private val timeNametagYOffset = Setting("TimeNametagYOffset", 0.8, 0.0, 3.0, 0.1) describedBy "The Y offset of the time nametag" subOf box visibleWhen { renderNametag.value }

    private val enemyRenderColour = Setting("EnemyFillColour", Color(185, 19, 255, 130)) describedBy "The colour of the fill" subOf box
    private val enemyRenderOutlineColour = Setting("EnemyOutlineColour", Color(185, 19, 255)) subOf box visibleWhen { renderMode.value != RenderMode.SHADER }
    private val friendRenderColour = Setting("FriendFillColour", Color(185, 19, 255, 130)) describedBy "The colour of the fill" subOf box
    private val friendRenderOutlineColour = Setting("FriendOutlineColour", Color(185, 19, 255)) subOf box visibleWhen { renderMode.value != RenderMode.SHADER }

    private val boxHeight = Setting("BoxHeight", 2.0, 0.0, 3.0, 0.1) describedBy "The height of the box" subOf box

    // List of players in the world, refreshed each tick
    private val playerSet = ConcurrentSet<EntityPlayer>()

    // List of logged players
    private val logged = ConcurrentHashMap<EntityPlayer, String>()

    // Shaders
    private val outlineShader = OutlineShader()
    private var frameBuffer: Framebuffer? = null
    private var lastScaleFactor = 0f
    private var lastScaleWidth = 0f
    private var lastScaleHeight = 0f

    private var lastDimension: Int = -Int.MAX_VALUE

    override fun onTick() {
        if (mc.anyNull) {
            // Clear if we aren't in a world
            playerSet.clear()
            logged.clear()

            return
        }

        if (mc.player.dimension != lastDimension) {
            lastDimension = mc.player.dimension

            playerSet.clear()
            logged.clear()

            return
        }

        // Refresh player set
        mc.world.playerEntities.filter { it != mc.player }.forEach { playerSet.add(it) }
    }

    override fun onRender3D() {
        logged.forEach { (player, date) ->
            // Do not render if they are far away
            if (player.getDistance(mc.player) >= range.value) {
                return@forEach
            }

            val playerPosition = BlockPos(floor(player.posX), floor(player.posY), floor(player.posZ))

            if (renderMode.value == RenderMode.BOX) {
                // Original box
                val originalBox = BlockUtil.getBlockBox(playerPosition)

                // Original box, with modified height
                val boundingBox: AxisAlignedBB = originalBox.setMaxY(originalBox.minY + boxHeight.value)

                // Render box
                RenderBuilder()
                    .boundingBox(boundingBox)
                    .inner(if (Paragon.INSTANCE.friendManager.isFriend(player.name)) friendRenderColour.value else enemyRenderColour.value)
                    .outer(if (Paragon.INSTANCE.friendManager.isFriend(player.name)) friendRenderOutlineColour.value else enemyRenderOutlineColour.value)
                    .type(box.value)
                    .start()
                    .lineWidth(width.value)
                    .blend(true)
                    .depth(true)
                    .texture(true)
                    .build(false)
            }

            val vec = if (renderMode.value == RenderMode.BOX) Vec3d(floor(player.posX) + 0.5, floor(player.posY), floor(player.posZ) + 0.5) else player.positionVector

            // Render name nametag
            if (renderNametag.value) {
                RenderUtil.drawNametagText(player.name, vec.add(Vec3d(0.0, nametagYOffset.value, 0.0)), Color.WHITE)
            }

            // Render time nametag
            if (timeNametag.value) {
                RenderUtil.drawNametagText(date, vec.add(Vec3d(0.0, timeNametagYOffset.value, 0.0)), Color.WHITE)
            }
        }
    }

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && renderMode.value == RenderMode.SHADER) {
            // Pretty much just taken from Cosmos, all credit goes to them (sorry linus!)
            // https://github.com/momentumdevelopment/cosmos/blob/main/src/main/java/cope/cosmos/client/features/modules/visual/ESPModule.java
            GlStateManager.enableAlpha()
            GlStateManager.pushMatrix()
            GlStateManager.pushAttrib()

            // Delete old framebuffer
            if (frameBuffer != null) {
                frameBuffer!!.framebufferClear()
                if (lastScaleFactor != event.resolution.scaleFactor.toFloat() || lastScaleWidth != event.resolution.scaledWidth.toFloat() || lastScaleHeight != event.resolution.scaledHeight.toFloat()) {
                    frameBuffer!!.deleteFramebuffer()
                    frameBuffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
                    frameBuffer!!.framebufferClear()
                }
                lastScaleFactor = event.resolution.scaleFactor.toFloat()
                lastScaleWidth = event.resolution.scaledWidth.toFloat()
                lastScaleHeight = event.resolution.scaledHeight.toFloat()
            }
            else {
                frameBuffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
            }

            frameBuffer!!.bindFramebuffer(false)
            val previousShadows = mc.gameSettings.entityShadows
            mc.gameSettings.entityShadows = false
            (mc.entityRenderer as IEntityRenderer).hookSetupCameraTransform(event.partialTicks, 0)

            logged.forEach { (player, _) ->
                // Do not render if they are far away
                if (player.getDistance(mc.player) >= range.value) {
                    return@forEach
                }

                mc.renderManager.renderEntityStatic(player, event.partialTicks, false)
            }

            mc.gameSettings.entityShadows = previousShadows
            GlStateManager.enableBlend()
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            frameBuffer!!.unbindFramebuffer()
            mc.framebuffer.bindFramebuffer(true)
            mc.entityRenderer.disableLightmap()
            RenderHelper.disableStandardItemLighting()
            GlStateManager.pushMatrix()

            // Render shader
            outlineShader.setColour(friendRenderColour.value)
            outlineShader.setWidth(width.value)
            outlineShader.setFill(fill.value.toBinary())
            outlineShader.setOutline(outline.value.toBinary())
            outlineShader.startShader()

            mc.entityRenderer.setupOverlayRendering()

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer!!.framebufferTexture)
            GL11.glBegin(GL11.GL_QUADS)
            GL11.glTexCoord2d(0.0, 1.0)
            GL11.glVertex2d(0.0, 0.0)
            GL11.glTexCoord2d(0.0, 0.0)
            GL11.glVertex2d(0.0, event.resolution.scaledHeight.toDouble())
            GL11.glTexCoord2d(1.0, 0.0)
            GL11.glVertex2d(event.resolution.scaledWidth.toDouble(), event.resolution.scaledHeight.toDouble())
            GL11.glTexCoord2d(1.0, 1.0)
            GL11.glVertex2d(event.resolution.scaledWidth.toDouble(), 0.0)
            GL11.glEnd()

            // Stop drawing shader
            GL20.glUseProgram(0)
            GL11.glPopMatrix()
            mc.entityRenderer.enableLightmap()

            GlStateManager.popMatrix()
            GlStateManager.popAttrib()

            mc.entityRenderer.setupOverlayRendering()
        }
    }

    @Listener
    fun onPlayerJoin(event: PlayerEvent.PlayerJoinEvent) {
        if (mc.anyNull) {
            return
        }

        logged.entries.removeIf { (player, _) ->
            // If the entry is the player joining
            if (player.name.equals(event.name)) {
                // Notify ourselves
                if (notify.value && logIn.value) {
                    Paragon.INSTANCE.notificationManager.addNotification(Notification("${player.name} has logged back in!", logInType.value))
                }

                // Remove from list
                true
            }
            else {
                false
            }
        }
    }

    @Listener
    fun onPlayerLeave(event: PlayerEvent.PlayerLeaveEvent) {
        if (mc.anyNull) {
            return
        }

        playerSet.removeIf {
            // If the player leaving is in the player set
            if (it.name.equals(event.name)) {
                // Notify ourselves
                if (notify.value && logOut.value) {
                    Paragon.INSTANCE.notificationManager.addNotification(Notification("${it.name} has logged out!", logOutType.value))
                }

                // Put player in logged map, and set the time
                logged[it] = SimpleDateFormat("k:mm").format(Date())
                true
            }
            else {
                false
            }
        }
    }

    enum class RenderMode {
        /**
         * Render a box
         */
        BOX,

        /**
         * Render a model
         */
        SHADER
    }

}
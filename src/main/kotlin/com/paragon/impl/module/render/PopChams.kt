package com.paragon.impl.module.render

import com.paragon.impl.event.combat.TotemPopEvent
import com.paragon.impl.event.render.entity.RenderEntityEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IEntityPlayer
import com.paragon.util.mc
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object PopChams : Module("PopChams", Category.RENDER, "PopChams duh") {

    private val renderStyle = Setting(
        "Style", Style.WIREFRAME
    )

    private val aliveTime = Setting(
        "Time", 2000F, 50F, 5000F, 50F
    ) describedBy "Time a cham will be shown"

    private val move = Setting("Move", true) describedBy "Move the cham after its creation"
    private val direction = Setting(
        "Direction", MovementDirection.UP
    ) describedBy "Direction to move the cham" subOf move
    private val movementHeight = Setting(
        "Height", 4.5F, 2.0F, 10F, 0.1F
    ) describedBy "Height that the chams will travel" subOf move

    private val easing = Setting(
        "Easing", Easing.QUINT_IN
    ) describedBy "Will be used for fading and moving" visibleWhen { fadeOut.value || move.value }
    private val outlineColor = Setting("Color", Colours.mainColour.value)
    private val outlineWidth = Setting("Width", 3.5F, 0.1F, 10F, 0.1F)
    private val fadeOut = Setting("Fade", true)
    private val self = Setting("Self", false)

    private val chams: MutableList<ChamData> = CopyOnWriteArrayList()

    private val chamCache: MutableMap<EntityPlayer, ChamData> = ConcurrentHashMap()

    override fun onTick() {
        chams.removeIf { System.currentTimeMillis() - it.startTime > aliveTime.value }
    }

    @Listener
    fun onPop(event: TotemPopEvent) {
        if (!self.value && event.player == mc.player) {
            return
        }

        val cham = chamCache[event.player] ?: return
        chams.add(
            ChamData(
                cham.model, EntityOtherPlayerMP( //Copying so we don't have new animations or anything
                    cham.entity.world, (cham.entity as IEntityPlayer).hookGetGameProfile()
                ).also { it.copyLocationAndAnglesFrom(cham.entity) }, cham.limbSwing, cham.limbSwingAmount, cham.ageInTicks, cham.netHeadYaw, cham.headPitch, cham.scale
            )
        )
    }

    @Listener
    fun onRenderEntity(event: RenderEntityEvent) {
        if (event.entity is EntityPlayer) {
            chamCache[event.entity] = ChamData(
                event.modelBase as ModelPlayer,
                event.entity,
                event.limbSwing,
                event.limbSwingAmount,
                event.ageInTicks,
                event.netHeadYaw,
                event.headPitch,
                event.scale
            )
        }
    }

    override fun onRender3D() {
        chams.forEach {
            it.animation.state = true
            val animFac = it.animation.getAnimationFactor()

            mc.renderManager.isRenderShadow = false

            glPushMatrix()

            //Positioning
            glTranslated(
                (mc.renderManager.viewerPosX - it.entity.posX) * -1.0, 1.4 + ((mc.renderManager.viewerPosY - it.entity.posY) * -1.0), (mc.renderManager.viewerPosZ - it.entity.posZ) * -1.0
            )

            if (move.value) {
                glTranslated(
                    0.0, (if (direction.value == MovementDirection.DOWN) -movementHeight.value else movementHeight.value) * animFac, 0.0
                )
            }

            //Flipping and setting the correct rotation and scale
            glRotatef(180F, 1F, 0F, 0F)
            glRotatef(-it.netHeadYaw, 0F, 1F, 0F)
            glScalef(0.95F, 0.95F, 0.95F)

            glAlphaFunc(GL_GREATER, 0.015686274F)

            glPushMatrix()
            glPushAttrib(GL_ALL_ATTRIB_BITS)

            val colour = Color(
                outlineColor.value.red / 255F, outlineColor.value.green / 255F, outlineColor.value.blue / 255F,

                if (fadeOut.value) {
                    1.0 - animFac.coerceIn(0.0, 1.0)
                }
                else {
                    outlineColor.value.alpha / 255.0
                }.toFloat()
            )

            glColor4f(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)

            glPolygonMode(GL_FRONT_AND_BACK, if (renderStyle.value == Style.FILL) GL_FILL else GL_LINE)
            glDisable(GL_TEXTURE_2D)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)

            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
            GlStateManager.glLineWidth(outlineWidth.value)

            glEnable(GL_DEPTH_TEST)
            glDepthMask(false)

            glDepthRange(0.1, 1.0)
            glDepthFunc(GL_GREATER)

            glColor4f(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)

            it.model.render(
                it.entity, it.limbSwing, it.limbSwingAmount, it.ageInTicks, it.netHeadYaw, it.headPitch, it.scale
            )

            glDepthFunc(GL_LESS)
            glDepthRange(0.0, 1.0)
            glEnable(GL_DEPTH_TEST)
            glDepthMask(false)

            glColor4f(colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)

            it.model.render(
                it.entity, it.limbSwing, it.limbSwingAmount, it.ageInTicks, it.netHeadYaw, it.headPitch, it.scale
            )

            glPopAttrib()
            glPopMatrix()
            glPopMatrix()
            mc.renderManager.isRenderShadow = mc.gameSettings.entityShadows
        }
    }

    internal class ChamData(
        val model: ModelPlayer,
        val entity: Entity,
        val limbSwing: Float,
        val limbSwingAmount: Float,
        val ageInTicks: Float,
        val netHeadYaw: Float,
        val headPitch: Float,
        val scale: Float,
        val startTime: Long = System.currentTimeMillis(),
        val animation: Animation = Animation(aliveTime::value, false, easing::value),
    )

    internal enum class MovementDirection {
        UP, DOWN
    }

    internal enum class Style {
        WIREFRAME, FILL
    }

}
package com.paragon.impl.module.render

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IEntityRenderer
import com.paragon.util.mc
import com.paragon.util.render.shader.shaders.*
import com.paragon.util.string.StringUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityEnderChest
import net.minecraft.tileentity.TileEntityShulkerBox
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20
import java.awt.Color

@SideOnly(Side.CLIENT)
object Shader : Module("Shader", Category.RENDER, "Apply a shader to entities and storages") {

    private val passive = Setting(
        "Passive", true
    ) describedBy "Apply shader to passive entities"

    private val mobs = Setting(
        "Mobs", true
    ) describedBy "Apply shader to hostile entities"

    private val players = Setting(
        "Players", true
    ) describedBy "Apply shader to player entities"

    private val crystals = Setting(
        "Crystals", true
    ) describedBy "Apply shader to crystals"

    private val items = Setting(
        "Items", true
    ) describedBy "Apply shader to items"

    private val chests = Setting(
        "Chests", true
    ) describedBy "Apply shader to chests"

    private val shulkers = Setting(
        "Shulkers", true
    ) describedBy "Apply shader to shulkers"

    private val enderChests = Setting(
        "Ender Chests", true
    ) describedBy "Apply shader to ender chests"

    private val shaderType = Setting(
        "FragmentShader", FragmentShader.DIAMONDS
    ) describedBy "The shader to use"

    // Diamonds
    private val diamondSpacing = Setting(
        "Spacing", 4f, 1f, 16f, 0.5f
    ) describedBy "The spacing between diamonds" subOf shaderType visibleWhen { shaderType.value == FragmentShader.DIAMONDS }

    private val diamondSize = Setting(
        "Size", 1f, 0.1f, 10f, 0.1f
    ) describedBy "The size of the diamonds" subOf shaderType visibleWhen { shaderType.value == FragmentShader.DIAMONDS }

    // Outline
    private val outlineWidth = Setting(
        "Width", 1f, 1f, 5f, 0.5f
    ) subOf shaderType visibleWhen { shaderType.value == FragmentShader.OUTLINE }

    private val outlineFill = Setting(
        "Fill", true
    ) describedBy "Fill the outline" subOf shaderType visibleWhen { shaderType.value == FragmentShader.OUTLINE }

    // Diagonal
    private val diagonalSpacing = Setting(
        "Spacing", 4f, 1f, 16f, 0.5f
    ) describedBy "The spacing between lines" subOf shaderType visibleWhen { shaderType.value == FragmentShader.DIAGONAL }

    private val diagonalWidth = Setting(
        "Width", 1f, 1f, 16f, 0.5f
    ) describedBy "The width of the lines" subOf shaderType visibleWhen { shaderType.value == FragmentShader.DIAGONAL }

    // Colour
    private val colour = Setting(
        "Colour", Color(185, 17, 255)
    ) describedBy "The colour of the shader"

    private val outlineShader = OutlineShader()
    private val diagonalShader = DiagonalShader()
    private val diamondsShader = DiamondsShader()
    private val fluidShader = FluidShader()
    private val liquidShader = LiquidShader()
    private val smokeShader = SmokeShader()
    private var frameBuffer: Framebuffer? = null
    private var lastScaleFactor = 0f
    private var lastScaleWidth = 0f
    private var lastScaleHeight = 0f

    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
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

            for (entity in mc.world.loadedEntityList) {
                if (entity != null && entity !== mc.player && isEntityValid(entity)) {
                    mc.renderManager.renderEntityStatic(entity, event.partialTicks, false)
                }
            }

            for (tileEntity in mc.world.loadedTileEntityList) {
                if (isStorageValid(tileEntity)) {
                    val x = mc.renderManager.viewerPosX
                    val y = mc.renderManager.viewerPosY
                    val z = mc.renderManager.viewerPosZ
                    TileEntityRendererDispatcher.instance.render(
                        tileEntity, tileEntity.pos.x - x, tileEntity.pos.y - y, tileEntity.pos.z - z, mc.renderPartialTicks
                    )
                }
            }

            mc.gameSettings.entityShadows = previousShadows
            GlStateManager.enableBlend()
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            frameBuffer!!.unbindFramebuffer()
            mc.framebuffer.bindFramebuffer(true)
            mc.entityRenderer.disableLightmap()
            RenderHelper.disableStandardItemLighting()
            GlStateManager.pushMatrix()

            when (shaderType.value) {
                FragmentShader.DIAMONDS -> {
                    diamondsShader.setColor(colour.value)
                    diamondsShader.setSpacing(diamondSpacing.value)
                    diamondsShader.setSize(diamondSize.value)
                    diamondsShader.startShader()
                }

                FragmentShader.OUTLINE -> {
                    outlineShader.setColour(colour.value)
                    outlineShader.setWidth(outlineWidth.value)
                    outlineShader.setFill(if (outlineFill.value) 1 else 0)
                    outlineShader.setOutline(1)
                    outlineShader.startShader()
                }

                FragmentShader.DIAGONAL -> {
                    diagonalShader.setColour(colour.value)
                    diagonalShader.setWidth(diagonalWidth.value)
                    diagonalShader.setSpacing(diagonalSpacing.value)
                    diagonalShader.startShader()
                }

                FragmentShader.FLUID -> {
                    fluidShader.time = fluidShader.time + 0.01
                    fluidShader.startShader()
                }

                FragmentShader.LIQUID -> {
                    liquidShader.time = liquidShader.time + 0.01
                    liquidShader.setColour(colour.value)
                    liquidShader.startShader()
                }

                FragmentShader.SMOKE -> {
                    smokeShader.time = smokeShader.time + 0.001f
                    smokeShader.setColour(colour.value)
                    smokeShader.startShader()
                }
            }

            mc.entityRenderer.setupOverlayRendering()
            glBindTexture(GL_TEXTURE_2D, frameBuffer!!.framebufferTexture)
            glBegin(GL_QUADS)
            glTexCoord2d(0.0, 1.0)
            glVertex2d(0.0, 0.0)
            glTexCoord2d(0.0, 0.0)
            glVertex2d(0.0, event.resolution.scaledHeight.toDouble())
            glTexCoord2d(1.0, 0.0)
            glVertex2d(event.resolution.scaledWidth.toDouble(), event.resolution.scaledHeight.toDouble())
            glTexCoord2d(1.0, 1.0)
            glVertex2d(event.resolution.scaledWidth.toDouble(), 0.0)
            glEnd()

            // Stop drawing shader
            GL20.glUseProgram(0)
            glPopMatrix()
            mc.entityRenderer.enableLightmap()
            mc.entityRenderer.setupOverlayRendering()
            GlStateManager.popMatrix()
            GlStateManager.popAttrib()
        }
    }

    private fun isEntityValid(entityIn: Entity): Boolean {
        return entityIn is EntityPlayer && players.value || entityIn is EntityLiving && entityIn !is EntityMob && passive.value || entityIn is EntityMob && mobs.value || entityIn is EntityEnderCrystal && crystals.value || entityIn is EntityItem && items.value
    }

    private fun isStorageValid(tileEntity: TileEntity?): Boolean {
        return when (tileEntity) {
            is TileEntityChest -> chests.value
            is TileEntityShulkerBox -> shulkers.value
            is TileEntityEnderChest -> enderChests.value
            else -> false
        }
    }

    override fun getData(): String = StringUtil.getFormattedText(shaderType.value)

    enum class FragmentShader {
        /**
         * Outline shader
         */
        OUTLINE,

        /**
         * Diagonal shader
         */
        DIAGONAL,

        /**
         * Diamonds shader
         */
        DIAMONDS,

        /**
         * Fluid Shader
         */
        FLUID,

        /**
         * Liquid shader
         */
        LIQUID,

        /**
         * Smoke shader
         */
        SMOKE
    }

}
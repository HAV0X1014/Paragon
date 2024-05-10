package com.paragon.impl.module.render

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.mc
import com.paragon.util.render.BlurUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.gui.ScaledResolution
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author Surge
 * @since 20/11/2022
 */
object Blur : Module("Blur", Category.RENDER, "Blurs the background of GUIS") {

    private val intensity = Setting("Intensity", 10f, 1f, 20f, 1f) describedBy "How intense the blur is"
    private val animationSpeed = Setting("AnimationSpeed", 200f, 0f, 500f, 5f) describedBy "The speed of the fade in animation"
    private val easing = Setting("Easing", Easing.LINEAR) describedBy "The easing type of the animation" excludes Easing.BACK_IN excludes Easing.BACK_IN_OUT

    private val fade = Animation({ animationSpeed.value }, false, { easing.value })

    override fun onTick() {
        fade.state = mc.currentScreen != null && mc.currentScreen != BetterScreenshot.GuiScreenshot
    }

    @SubscribeEvent
    fun onHotbarRender(event: RenderGameOverlayEvent.Post) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR && fade.getAnimationFactor() > 0) {
            val scaledResolution = ScaledResolution(mc)

            BlurUtil.blur(0f, 0f, scaledResolution.scaledWidth.toFloat(), scaledResolution.scaledHeight.toFloat(), (intensity.value * fade.getAnimationFactor()).toInt().toFloat())
        }
    }
}
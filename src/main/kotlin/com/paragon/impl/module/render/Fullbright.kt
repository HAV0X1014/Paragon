package com.paragon.impl.module.render

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.string.StringUtil
import net.minecraft.init.MobEffects
import net.minecraft.potion.PotionEffect

/**
 * @author Surge
 * @since 06/07/22
 */
object Fullbright : Module("Fullbright", Category.RENDER, "Changes your brightness beyond vanilla values") {

    private val mode = Setting(
        "Mode", Mode.GAMMA
    ) describedBy "The mode to use for the brightness"

    private var originalGamma = 0F

    override fun onEnable() {
        // Set original gamma to current
        this.originalGamma = mc.gameSettings.gammaSetting
    }

    override fun onDisable() {
        // Reset gamma to original
        mc.gameSettings.gammaSetting = this.originalGamma

        // Remove night vision
        if (!mc.anyNull) {
            mc.player.removePotionEffect(MobEffects.NIGHT_VISION)
        }
    }

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        when (mode.value) {
            Mode.GAMMA -> mc.gameSettings.gammaSetting = 50000F
            Mode.EFFECT -> {
                // Apply night vision
                if (!mc.player.isPotionActive(MobEffects.NIGHT_VISION)) {
                    mc.player.addPotionEffect(PotionEffect(MobEffects.NIGHT_VISION, 999999, 255))
                }
            }

            Mode.ANTI -> mc.gameSettings.gammaSetting = -Float.MAX_VALUE // Decrease gamma
        }
    }

    override fun getData(): String = StringUtil.getFormattedText(mode.value)

    enum class Mode {
        /**
         * Change gamma value
         */
        GAMMA,

        /**
         * Apply night vision effect
         */
        EFFECT,

        /**
         * Reverse fullbright
         */
        ANTI
    }

}
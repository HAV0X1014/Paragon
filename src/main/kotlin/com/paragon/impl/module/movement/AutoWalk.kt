package com.paragon.impl.module.movement

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IKeyBinding
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.client.settings.KeyBinding

/**
 * @author Surge
 * @since 14/05/22
 */
object AutoWalk : Module("AutoWalk", Category.MOVEMENT, "Makes you constantly walk") {

    private val direction = Setting(
        "Direction", Direction.FORWARD
    ) describedBy "The direction to walk in"

    override fun onDisable() {
        if (mc.anyNull) {
            return
        }

        // Reset the key on disable
        (direction.value.key as IKeyBinding).hookSetPressed(false)
    }

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        // Set the key to pressed
        (direction.value.key as IKeyBinding).hookSetPressed(true)
    }

    enum class Direction(val key: KeyBinding) {
        /**
         * Walk forward
         */
        FORWARD(mc.gameSettings.keyBindForward),

        /**
         * Walk backward
         */
        BACKWARD(mc.gameSettings.keyBindBack),

        /**
         * Walk left
         */
        LEFT(mc.gameSettings.keyBindLeft),

        /**
         * Walk right
         */
        RIGHT(mc.gameSettings.keyBindRight);

    }

}
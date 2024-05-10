package com.paragon.impl.module.movement

import com.paragon.impl.event.input.KeybindingPressedEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiRepair
import net.minecraft.client.settings.KeyBinding
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Keyboard

/**
 * @author aesthetical, Surge
 * @since 07/14/2022
 */
@Aliases(["InventoryMove", "GuiMove", "GuiWalk"])
object InventoryWalk : Module("InventoryWalk", Category.MOVEMENT, "Lets you walk around in your inventory") {

    private val rotate = Setting(
        "Rotate", true
    ) describedBy "If you can use the arrow keys to rotate in your inventory"

    private val rotateSpeed = Setting(
        "Speed", 5f, 1f, 45f, 1f
    ) describedBy "How fast to rotate" subOf rotate

    private val bindings = arrayOf(
        mc.gameSettings.keyBindForward,
        mc.gameSettings.keyBindBack,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindRight,
        mc.gameSettings.keyBindJump,
        mc.gameSettings.keyBindSneak,
        mc.gameSettings.keyBindSprint
    )

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        if (isValidGUI) {
            for (binding in bindings) {
                KeyBinding.setKeyBindState(binding.keyCode, Keyboard.isKeyDown(binding.keyCode))
            }

            if (rotate.value) {
                if (Keyboard.isKeyDown(Keyboard.KEY_UP)) {
                    mc.player.rotationPitch -= rotateSpeed.value
                }
                else if (Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
                    mc.player.rotationPitch += rotateSpeed.value
                }
                else if (Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
                    mc.player.rotationYaw -= rotateSpeed.value
                }
                else if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
                    mc.player.rotationYaw += rotateSpeed.value
                }

                mc.player.rotationPitch = MathHelper.clamp(mc.player.rotationPitch, -90.0f, 90.0f)
            }
        }
    }

    @Listener
    fun onKeyBindingPressedOverride(event: KeybindingPressedEvent) {
        if (isValidGUI) {
            runCatching {
                event.pressedState = Keyboard.isKeyDown(event.keyCode)
                event.cancel()
            } //This only throws for mouse binds, which we'll ignore
        }
    }

    private val isValidGUI: Boolean
        get() = mc.currentScreen != null && mc.currentScreen !is GuiChat && mc.currentScreen !is GuiRepair

}
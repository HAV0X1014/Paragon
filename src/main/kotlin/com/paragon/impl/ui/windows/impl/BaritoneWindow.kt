package com.paragon.impl.ui.windows.impl

import baritone.api.BaritoneAPI
import com.paragon.Paragon
import com.paragon.impl.ui.util.Click
import com.paragon.impl.ui.windows.Window
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.util.math.MathHelper
import java.awt.Color
import java.util.function.Supplier
import kotlin.math.max

/**
 * @author Surge
 * @since 27/07/2022
 */
class BaritoneWindow(x: Float, y: Float, width: Float, height: Float, grabbableHeight: Float) : Window("Baritone", x, y, width, height, grabbableHeight) {

    private val buttons: ArrayList<Button> = arrayListOf(
        Button("RenderPath", { BaritoneAPI.getSettings().renderPath.value }, { BaritoneAPI.getSettings().renderPath.value = !BaritoneAPI.getSettings().renderPath.value }, x, y, width, height),
        Button("RenderGoal", { BaritoneAPI.getSettings().renderGoal.value }, { BaritoneAPI.getSettings().renderGoal.value = !BaritoneAPI.getSettings().renderGoal.value }, x, y, width, height),

        Button("AllowBreak", { BaritoneAPI.getSettings().allowBreak.value }, { BaritoneAPI.getSettings().allowBreak.value = !BaritoneAPI.getSettings().allowBreak.value }, x, y, width, height),
        Button("AllowPlace", { BaritoneAPI.getSettings().allowPlace.value }, { BaritoneAPI.getSettings().allowPlace.value = !BaritoneAPI.getSettings().allowPlace.value }, x, y, width, height),
        Button("Avoidance", { BaritoneAPI.getSettings().avoidance.value }, { BaritoneAPI.getSettings().avoidance.value = !BaritoneAPI.getSettings().avoidance.value }, x, y, width, height),
        Button("AllowSprint", { BaritoneAPI.getSettings().allowSprint.value }, { BaritoneAPI.getSettings().allowSprint.value = !BaritoneAPI.getSettings().allowSprint.value }, x, y, width, height),
        Button("AllowParkour", { BaritoneAPI.getSettings().allowParkour.value }, { BaritoneAPI.getSettings().allowParkour.value = !BaritoneAPI.getSettings().allowParkour.value }, x, y, width, height),
        Button("AllowWaterBucket", { BaritoneAPI.getSettings().allowWaterBucketFall.value }, { BaritoneAPI.getSettings().allowWaterBucketFall.value = !BaritoneAPI.getSettings().allowWaterBucketFall.value }, x, y, width, height),
        Button("AssumeLavaWalking", { BaritoneAPI.getSettings().assumeWalkOnLava.value }, { BaritoneAPI.getSettings().assumeWalkOnLava.value = !BaritoneAPI.getSettings().assumeWalkOnLava.value }, x, y, width, height),
        Button("AllowWater", { BaritoneAPI.getSettings().okIfWater.value }, { BaritoneAPI.getSettings().okIfWater.value = !BaritoneAPI.getSettings().okIfWater.value }, x, y, width, height),
        Button("AllowDownward", { BaritoneAPI.getSettings().allowDownward.value }, { BaritoneAPI.getSettings().allowDownward.value = !BaritoneAPI.getSettings().allowDownward.value }, x, y, width, height),
        Button("AllowJumpAt256", { BaritoneAPI.getSettings().allowJumpAt256.value }, { BaritoneAPI.getSettings().allowJumpAt256.value = !BaritoneAPI.getSettings().allowJumpAt256.value }, x, y, width, height)
    )

    private var scroll = 0f

    override fun scroll(mouseX: Int, mouseY: Int, mouseDelta: Int): Boolean {
        if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in (y + 16f)..(y + height - 16f)) {
            if (mouseDelta != 0) {
                scroll += 18 * if (mouseDelta > 0) 1 else -1
                return true
            }
        }

        return super.scroll(mouseX, mouseY, mouseDelta)
    }

    override fun draw(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        if (scroll > 0) {
            scroll = 0f
        }

        RenderUtil.pushScissor(x, y + grabbableHeight + 1, width, height - grabbableHeight)

        scroll = MathHelper.clamp(scroll.toDouble(), -max(0.0, ((buttons.sumOf { it.height.toDouble() + 2f }) - height + grabbableHeight + 6)), 0.0).toFloat()

        var offset = 0f
        buttons.forEach {
            it.x = x + 3
            it.y = (y + grabbableHeight + 3f) + offset + scroll
            it.width = width - 6f
            it.height = 16f

            it.draw(mouseX, mouseY)

            offset += it.height + 2f
        }

        RenderUtil.popScissor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
        if (mouseX.toFloat() in x + width - FontUtil.getStringWidth("X") - 5..x + width && mouseY.toFloat() in y..y + grabbableHeight) {
            Paragon.INSTANCE.configurationGUI.removeBuffer.add(this)
            return true
        }

        if (mouseY.toFloat() in y + grabbableHeight..y + (height - 19f)) {
            buttons.forEach {
                it.clicked(mouseX, mouseY, click)
            }
        }

        // dragging
        val superValue = super.mouseClicked(mouseX, mouseY, click)

        if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + grabbableHeight + height) {
            return true
        }

        return superValue
    }

    class Button(val name: String, val getter: Supplier<Boolean>, val setter: Runnable, var x: Float, var y: Float, var width: Float, var height: Float) {
        fun draw(mouseX: Int, mouseY: Int) {
            val hovered = mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height

            RenderUtil.drawRect(x, y, width, height, if (hovered) Color(0, 0, 0, 120) else Color(0, 0, 0, 150))
            FontUtil.drawStringWithShadow(name, x + 3, y + 4, Color.WHITE)

            RenderUtil.drawRect(x + width - 16f, y, 16f, height, Color(0, 0, 0, 150))

            if (getter.get()) {
                RenderUtil.scaleTo(x + width - 12.5f, y + 2f, 0f, 0.6, 0.6, 0.6) {
                    FontUtil.drawIcon(FontUtil.Icon.TICK, x + width - 14.5f , y + 2f, Color.WHITE)
                }
            }
        }

        fun clicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
            if (click == Click.LEFT && mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height) {
                setter.run()
                return true
            }

            return false
        }
    }

}
package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.combat.Aura
import com.paragon.impl.module.combat.AutoCrystal
import com.paragon.impl.module.hud.HUDModule
import com.paragon.util.mc
import com.paragon.util.render.BlurUtil
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.scaleTo
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.entity.EntityLivingBase
import java.awt.Color
import kotlin.math.max

/**
 * @author Surge
 * @since 25/12/2022
 */
object TargetHUD : HUDModule(
    "TargetHUD",
    "Draws information about the current target to the screen",
    {
        max(57f, if (TargetHUD.target == null) 0f else FontUtil.getStringWidth(TargetHUD.target!!.name))
    },
    {
        if (TargetHUD.target == null) {
            FontUtil.getHeight() + 2f
        } else {
            38f
        }
    }
) {

    private var target: EntityLivingBase? = null

    override fun draw() {
        val possible = arrayOf(
            AutoCrystal.crystal?.target,
            Aura.lastTarget
        )

        if (possible.any { it != null }) {
            target = possible.first { it != null }
        }

        if (target == null) {
            return
        }

        BlurUtil.blur(x, y, width.invoke(), height.invoke(), 5f)
        RenderUtil.drawRect(x, y, width.invoke(), height.invoke(), Color(0, 0, 0, 150))

        FontUtil.drawString(target!!.name, x + 38f, x + 5f, Color.WHITE)

        Minecraft.getMinecraft().textureManager.bindTexture(mc.connection!!.getPlayerInfo(target!!.uniqueID).locationSkin)
        Gui.drawModalRectWithCustomSizedTexture(x.toInt() + 5, y.toInt() + 5, 28f, 28f, 28, 28, 225F, 225F)

        scaleTo(x + 38F, y + 10F + FontUtil.getHeight(), 1F, 0.7, 0.7, 0.7) {
            target!!.armorInventoryList.forEachIndexed { i, armor ->
                RenderUtil.drawItemStack(armor, x + 38F + (18F * i), y + 16F + FontUtil.getHeight(), true)
            }
        }

        val healthFactor = target!!.health / (target as EntityLivingBase).maxHealth

        RenderUtil.drawRect(x + 38f, y + 16f, 50f, 7f, Color(50, 50, 55))
        RenderUtil.drawRect(x + 38f, y + 16f, 50f * healthFactor, 7f, Color(255 - (255 * healthFactor).toInt(), (255 * healthFactor).toInt(), 0))
    }

    override fun dummy() {
        if (target != null) {
            draw()

            return
        }

        FontUtil.drawString("TargetHUD", x + 1, y + 1, Color.WHITE)
    }

}
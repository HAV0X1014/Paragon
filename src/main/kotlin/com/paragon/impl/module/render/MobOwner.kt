package com.paragon.impl.module.render

import com.paragon.impl.module.Module
import com.paragon.impl.module.Category
import com.paragon.util.entity.EntityUtil
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil.drawNametagText
import net.minecraft.entity.IEntityOwnable
import net.minecraft.entity.passive.AbstractHorse
import net.minecraft.util.math.Vec3d
import java.awt.Color

object MobOwner : Module("MobOwner", Category.RENDER, "Displays the owner of a mob") {

    override fun onRender3D() {
        mc.world.loadedEntityList.forEach {
            if (it is IEntityOwnable) {
                val name = if (it.customNameTag == "") it.name else it.customNameTag
                val owner = if ((it as IEntityOwnable).owner == null) {
                    "No Owner"
                }
                else (it as IEntityOwnable).owner?.name

                val vec = EntityUtil.getInterpolatedPosition(it)

                if (it is AbstractHorse) {
                    drawNametagText(
                        "Name: " + name + ", Speed: " + (it as AbstractHorse).aiMoveSpeed + ", Owner: " + owner, Vec3d(vec.x, vec.y + 1.25, vec.z), Color.WHITE
                    )
                }
                else {
                    drawNametagText("Name: $name, Owner: $owner", Vec3d(vec.x, vec.y + 1.25, vec.z), Color.WHITE)
                }
            }
        }
    }

}
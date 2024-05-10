package com.paragon.impl.event.render.entity

import com.paragon.bus.event.Event
import net.minecraft.entity.player.EntityPlayer

/**
 * @author Surge
 * @since 01/08/2022
 */
open class RenderArmEvent(val player: EntityPlayer, val useSmallArms: Boolean) : Event() {

    class LeftArmPre(player: EntityPlayer, useSmallArms: Boolean) : RenderArmEvent(player, useSmallArms)
    class LeftArmPost(player: EntityPlayer, useSmallArms: Boolean) : RenderArmEvent(player, useSmallArms)
    class RightArmPre(player: EntityPlayer, useSmallArms: Boolean) : RenderArmEvent(player, useSmallArms)
    class RightArmPost(player: EntityPlayer, useSmallArms: Boolean) : RenderArmEvent(player, useSmallArms)

}
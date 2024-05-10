package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.InventoryUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Mouse

/**
 * @author Surge
 */
@Aliases(["MiddleClickFriend", "MCF", "MiddleClickPearl", "MCP"])
object MiddleClick : Module("MiddleClick", Category.MISC, "Allows you to perform actions when you middle click") {

    private val friend = Setting(
        "Friend", true
    ) describedBy "Add a friend when you middle click on an player"
    private val pearl = Setting(
        "Pearl", true
    ) describedBy "Throw an ender pearl when you miss an entity"

    // To prevent excessive spam
    private var hasClicked = false

    @SubscribeEvent
    fun onMouseInput(event: InputEvent.MouseInputEvent?) {
        if (mc.anyNull) {
            return
        }

        // Check that middle click button is pressed, and we haven't just clicked
        if (Mouse.isButtonDown(2)) {
            val result = mc.objectMouseOver.typeOfHit
            if (!hasClicked) {
                // If the type of hit is a player
                if (result == RayTraceResult.Type.ENTITY && mc.objectMouseOver.entityHit is EntityPlayer && friend.value) {
                    // Create new player object
                    val player = mc.objectMouseOver.entityHit.name

                    if (Paragon.INSTANCE.friendManager.isFriend(player)) {
                        // Remove player from social list
                        Paragon.INSTANCE.friendManager.removePlayer(player)

                        Paragon.INSTANCE.commandManager.sendClientMessage("${TextFormatting.RED}Removed player ${TextFormatting.GRAY}$player ${TextFormatting.RED}from your socials list!")
                    } else {
                        // Add player to social list
                        Paragon.INSTANCE.friendManager.addName(player)

                        Paragon.INSTANCE.commandManager.sendClientMessage("${TextFormatting.GREEN}Added player ${TextFormatting.GRAY}$player${TextFormatting.GREEN} to your friends list!")
                    }
                } else if (pearl.value) {
                    // The last slot we were on
                    val prevSlot = mc.player.inventory.currentItem

                    // Switch to pearl, if we can
                    if (InventoryUtil.switchToItem(Items.ENDER_PEARL, false)) {
                        // Throw pearl
                        mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND)

                        // Switch back to old slot
                        InventoryUtil.switchToSlot(prevSlot, false)
                    }
                }
            }

            // We have clicked
            hasClicked = true
        } else {
            // Reset hasClicked
            hasClicked = false
        }
    }

}
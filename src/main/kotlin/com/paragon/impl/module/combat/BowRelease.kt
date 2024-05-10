package com.paragon.impl.module.combat

import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.math.BlockPos

/**
 * @author Surge
 */
@Aliases(["AutoBowRelease"])
object BowRelease : Module("BowRelease", Category.COMBAT, "Automatically releases your bow when at max charge") {

    private val release = Setting("Release", Release.TICKS) describedBy "When to release the bow"
    private val releasePower = Setting(
        "Power", 3.1f, 0.1f, 4.0f, 0.1f
    ) describedBy "The power the bow needs to be before releasing" visibleWhen { release.value == Release.POWER }
    private val releaseTicks = Setting(
        "Ticks", 3.0f, 0.0f, 60.0f, 1.0f
    ) describedBy "The amount of ticks that have passed before releasing" visibleWhen { release.value == Release.TICKS }

    private var ticks = 0

    override fun onTick() {
        if (mc.anyNull || mc.player.heldItemMainhand.item !== Items.BOW) {
            return
        }

        if (!mc.player.isHandActive || mc.player.itemInUseMaxCount < 3) {
            return
        }

        when (release.value) {
            Release.POWER -> {
                // Get the charge power (awesome logic from trajectories!)
                val power: Float = ((72000 - mc.player.itemInUseCount) / 20.0f * ((72000 - mc.player.itemInUseCount) / 20.0f) + (72000 - mc.player.itemInUseCount) / 20.0f * 2.0f) / 3.0f * 3

                // Return if the power is not high enough
                if (power < releasePower.value) {
                    return
                }
            }

            Release.TICKS -> if (ticks++ < releaseTicks.value) return
        }


        // Release the bow
        mc.player.connection.sendPacket(
            CPacketPlayerDigging(
                CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, mc.player.horizontalFacing
            )
        )
        mc.player.connection.sendPacket(CPacketPlayerTryUseItem(mc.player.activeHand))
        mc.player.stopActiveHand()

        // Set ticks back to 0
        ticks = 0
    }

    enum class Release {
        /**
         * Release on specified power
         */
        POWER,

        /**
         * Release on amount of ticks
         */
        TICKS
    }

}
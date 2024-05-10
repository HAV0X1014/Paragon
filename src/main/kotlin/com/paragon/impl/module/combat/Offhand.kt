package com.paragon.impl.module.combat

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.mixins.accessor.IEntityPlayerSP
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.combat.CrystalUtil.getDamageToEntity
import com.paragon.util.entity.EntityUtil
import com.paragon.util.mc
import com.paragon.util.player.InventoryUtil
import com.paragon.util.world.BlockUtil
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType
import net.minecraft.item.Item
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.util.math.BlockPos
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.math.floor

/**
 * @author Surge
 * @since 24/11/2022
 */
@Aliases(["AutoTotem"])
object Offhand : Module("Offhand", Category.COMBAT, "Automatically manages your offhand") {

    // General settings
    private val delay = Setting("Delay", 0.0, 0.0, 100.0, 1.0) describedBy "The delay between switching items"
    private val allowMerging = Setting("AllowMerging", true) describedBy "Allow returning items to be merged"

    // Item settings
    private val priority = Setting("Priority", EnumItem.CRYSTAL) describedBy "The item to prioritise"
    private val fallback = Setting("Fallback", EnumItem.GAPPLE) describedBy "The item to swap to when we don't have the priority item"

    // Key swap settings
    private val keySwap = Setting("KeySwap", EnumItem.TOTEM) describedBy "The item to switch to when we hold down a key"
    private val keySwapKey = Setting("Key", Bind(0, Bind.Device.KEYBOARD)) subOf keySwap
    private val keySwapOverrideSafety = Setting("OverrideSafety", false) describedBy "Ignore safety when holding the key" subOf keySwap

    // Gapple settings
    private val dynamicGapple = Setting("DynamicGapple", DynamicGapple.SWORD) describedBy "When to switch to a gapple"
    private val allowCrapple = Setting("AllowCrapple", false) describedBy "Whether to allow non enchanted golden apples" visibleWhen { dynamicGapple.value != DynamicGapple.NEVER || priority.value == EnumItem.GAPPLE || fallback.value == EnumItem.GAPPLE || keySwap.value == EnumItem.GAPPLE }

    // Safety settings
    private val safety = Setting("Safety", true) describedBy "Switch to totems in certain scenarios"
    private val health = Setting("Health", true) describedBy "Switch to a totem when your health is low" subOf safety
    private val healthThreshold = Setting("HealthThreshold", 10f, 1f, 20f, 1f) describedBy "The health threshold" subOf safety visibleWhen { health.value }
    private val falling = Setting("Falling", false) describedBy "Switch to a totem when falling" subOf safety
    private val elytra = Setting("Elytra", false) describedBy "Switch to a totem when flying with an elytra" subOf safety
    private val lethalCrystal = Setting("LethalCrystal", true) describedBy "Switch to a totem when there is a lethal crystal nearby" subOf safety

    // Anticheat settings
    private val freezeMotion = Setting("FreezeMotion", false) describedBy "Stop motion whilst swapping"
    private val strictSprint = Setting("StrictSprint", false) describedBy "Force the player to stop sprinting whilst swapping"
    private val inventory = Setting("StrictInventory", Inventory.OPEN) describedBy "Spoof opening your inventory"

    // Pause after fail settings
    private val pauseAfterFail = Setting("PauseAfterFail", false) describedBy "Pause for a specified amount of ticks after failing to swap"
    private val pauseFailThreshold = Setting("FailThreshold", 3.0, 1.0, 5.0, 1.0) describedBy "The amount of times you have to fail before pausing"
    private val pauseTicks = Setting("Ticks", 1.0, 1.0, 5.0, 1.0) describedBy "The amount of ticks to pause for" subOf pauseAfterFail

    // Delay timer
    private val timer = Timer()

    // Pause After Fail fields
    private var checkOnNextTick = false
    private var failedAttempts = 0
    private var paused = 0

    override fun onTick() {
        if (mc.anyNull || mc.currentScreen != null) {
            return
        }

        // Gets the item we want to switch to
        val item = getItem()

        if (checkOnNextTick) {

            // If we haven't successfully swapped
            if (mc.player.heldItemOffhand.item != item.item) {
                failedAttempts++
            } else {
                timer.reset()
            }
        }

        // Rest swapping
        if (pauseAfterFail.value && failedAttempts >= pauseFailThreshold.value) {
            paused++

            if (paused < pauseTicks.value) {
                return
            }
        }

        // Successfully swapped
        if (mc.player.heldItemOffhand.item == item.item) {
            checkOnNextTick = false
            failedAttempts = 0
            paused = 0
            return
        }

        // The item's slot
        var slot = -1

        // Iterate through inventory slots
        for (i in 9..36) {

            // Current slot stack
            val itemInInv = mc.player.inventory.getStackInSlot(i)

            // It is our item
            if (itemInInv.item == item.item) {

                // Check crapple status
                if (item.item == Items.GOLDEN_APPLE && !allowCrapple.value && !itemInInv.hasEffect()) {
                    continue
                }

                slot = i
                break
            }
        }

        // Delay hasn't passed, or we don't have a correct slot
        if (!timer.hasMSPassed(delay.value) || slot == -1) {
            return
        }

        // Spoof inventory
        when (inventory.value) {
            Inventory.PACKET -> {
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.OPEN_INVENTORY))
            }

            Inventory.OPEN -> {
                mc.displayGuiScreen(GuiInventory(mc.player))
            }

            else -> {}
        }

        // The sprint state
        var sprinting = false

        if (strictSprint.value) {

            // Set sprint state
            sprinting = mc.player.isSprinting || (mc.player as IEntityPlayerSP).hookGetServerSprintState()

            if (sprinting) {

                // Force stop sprinting
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING))
            }
        }

        // Freeze the player's X and Z motion
        if (freezeMotion.value) {
            mc.player.setVelocity(0.0, mc.player.motionY, 0.0)
        }

        // Click on item slot
        mc.playerController.windowClick(0, slot, 0, ClickType.PICKUP, mc.player)

        // Click on offhand slot
        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player)

        // We don't need to find a return slot
        if (!mc.player.inventory.itemStack.isEmpty) {
            var returnSlot = -1

            for (i in 9 until 36) {
                val currentSlot = mc.player.inventoryContainer.inventory[i]
                val canMerge = allowMerging.value && currentSlot.displayName == mc.player.inventory.itemStack.displayName && currentSlot.item == mc.player.inventory.itemStack.item && (64 - currentSlot.count) >= mc.player.inventory.itemStack.count

                // The slot is empty
                if (currentSlot.isEmpty || canMerge) {
                    returnSlot = i
                    break
                }
            }

            if (returnSlot != -1) {

                // Click on return slot
                mc.playerController.windowClick(0, returnSlot, 0, ClickType.PICKUP, mc.player)
                mc.playerController.updateController()
            }
        }

        if (sprinting) {

            // Start sprinting again
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING))
        }

        // Spoof closing inventory
        when (inventory.value) {
            Inventory.PACKET -> {
                mc.player.connection.sendPacket(CPacketCloseWindow(mc.player.inventoryContainer.windowId))
            }

            Inventory.OPEN -> {
                mc.displayGuiScreen(null)
            }

            else -> {}
        }

        // Make us check the offhand on the next tick
        checkOnNextTick = true
    }

    private fun getItem(): EnumItem {
        // Whether we are in a dangerous situation
        val safety = shouldApplySafety()

        // Whether we are holding down our key swap key
        // This will return false if we need to apply safety and are not wanting to override safety...
        val keySwap = shouldApplyKeySwap(safety)

        // ...therefore, we don't have to check safety here
        if (keySwap) {
            return this.keySwap.value
        }

        // Return totem if we need to apply safety
        if (safety) {
            return EnumItem.TOTEM
        }

        // We want to swap to a gapple
        if (dynamicGapple.value != DynamicGapple.NEVER && when (dynamicGapple.value) {
                DynamicGapple.SWORD -> {
                    // Self-explanatory
                    InventoryUtil.isHoldingSword
                }

                DynamicGapple.HOLE -> {
                    // The floored position is a safe hole
                    BlockUtil.isSafeHole(BlockPos(floor(mc.player.posX), floor(mc.player.posY), floor(mc.player.posZ)), true)
                }

                else -> {
                    // Holding sword or in safe hole
                    InventoryUtil.isHoldingSword || BlockUtil.isSafeHole(BlockPos(floor(mc.player.posX), floor(mc.player.posY), floor(mc.player.posZ)), true)
                }
            }) {

            // Whether we:
            // A. Have a gapple
            // B. Want to allow crapples and such
            var hasValidGapple = false

            for (i in 9..35) {
                val itemInInv = mc.player.inventory.getStackInSlot(i)

                if (itemInInv.item === EnumItem.GAPPLE.item) {
                    // crapple checking
                    if (!allowCrapple.value && !itemInInv.hasEffect()) {
                        continue
                    }

                    hasValidGapple = true
                    break
                }
            }

            if (hasValidGapple) {
                return EnumItem.GAPPLE
            }
        }

        // If we do not have the priority item in our inventory, and the current item in our offhand isn't the priority item
        if (InventoryUtil.getCountOfItem(priority.value.item, hotbarOnly = false, ignoreHotbar = true) == 0 && mc.player.heldItemOffhand.item != priority.value.item) {
            return fallback.value
        }

        return priority.value
    }

    private fun shouldApplySafety(): Boolean {
        if (safety.value) {
            // Health is below threshold
            if (health.value && EntityUtil.getEntityHealth(mc.player) <= healthThreshold.value) {
                return true
            }

            // We are falling
            if (falling.value && mc.player.fallDistance >= 3) {
                return true
            }

            // We are flying
            if (elytra.value && mc.player.isElytraFlying) {
                return true
            }

            // We are in the vicinity of a lethal crystal
            if (lethalCrystal.value && mc.world.loadedEntityList.filterIsInstance<EntityEnderCrystal>().any { it.getDamageToEntity(mc.player) >= EntityUtil.getEntityHealth(mc.player) }) {
                return true
            }
        }

        return false
    }

    private fun shouldApplyKeySwap(safetyIn: Boolean): Boolean {
        if (mc.currentScreen != null) {
            return false
        }

        var keyPressed = false

        // isPressed() doesn't work :(
        if (keySwapKey.value.buttonCode != 0) {
            keyPressed = when (keySwapKey.value.device) {
                Bind.Device.KEYBOARD -> Keyboard.isKeyDown(keySwapKey.value.buttonCode)
                Bind.Device.MOUSE -> Mouse.isButtonDown(keySwapKey.value.buttonCode)
            }
        }

        if (keyPressed) {
            if (safetyIn && !keySwapOverrideSafety.value) {
                return false
            }

            return true
        }

        return false
    }

    enum class EnumItem(val item: Item) {
        /**
         * Switch to crystal
         */
        CRYSTAL(Items.END_CRYSTAL),

        /**
         * Switch to totem
         */
        TOTEM(Items.TOTEM_OF_UNDYING),

        /**
         * Switch to gapple
         */
        GAPPLE(Items.GOLDEN_APPLE)
    }

    enum class DynamicGapple {
        /**
         * Swap to gapple when holding a sword in your main hand
         */
        SWORD,

        /**
         * Swap to gapple when you're in a hole
         */
        HOLE,

        /**
         * Swap to gapple for both events
         */
        BOTH,

        /**
         * Never switch to gapples
         */
        NEVER
    }

    enum class Inventory {
        /**
         * Do not spoof opening the inventory
         */
        NONE,

        /**
         * Packet spoof
         */
        PACKET,

        /**
         * Actually open the GUI for less than a tick
         */
        OPEN
    }

}
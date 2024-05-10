package com.paragon.impl.module.combat

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.event.world.entity.EntityRemoveFromWorldEvent
import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.managers.rotation.Rotation
import com.paragon.impl.managers.rotation.RotationPriority
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.annotation.Constant
import com.paragon.impl.setting.Setting
import com.paragon.mixins.accessor.ICPacketUseEntity
import com.paragon.mixins.accessor.IEntityPlayerSP
import com.paragon.mixins.accessor.IPlayerControllerMP
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.entity.EntityUtil
import com.paragon.util.entity.EntityUtil.isMonster
import com.paragon.util.entity.EntityUtil.isPassive
import com.paragon.util.mc
import com.paragon.util.player.InventoryUtil
import com.paragon.util.player.PlayerUtil
import com.paragon.util.player.RotationUtil
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.world.BlockUtil
import com.paragon.util.world.BlockUtil.getBlockAtPos
import com.paragon.util.world.BlockUtil.getSurroundingBlocks
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.init.SoundEvents
import net.minecraft.item.ItemEndCrystal
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketSoundEffect
import net.minecraft.network.play.server.SPacketSpawnObject
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import java.awt.Color
import java.util.function.Function

/**
 * @author Surge
 * @since 28/08/2022
 */
@Aliases(["CrystalAura", "CA", "AC"])
object AutoCrystal : Module("AutoCrystal", Category.COMBAT, "Automatically places and explodes crystals") {

    /************************ General Settings ************************/
    private val order = Setting("Order", Order.PLACE_EXPLODE) describedBy "Which order to perform actions in"
    private val timing = Setting("Timing", Timing.SEQUENTIAL) describedBy "When to perform actions"
    private val damage = Setting("Damage", Damage.PLAIN) describedBy "The algorithm to calculate damage"

    /************************ Rotation Settings ************************/
    private val rotate = Setting("Rotate", RotateOn.PLACE) describedBy "When to rotate"
    private val rotateType = Setting("Type", Rotate.PACKET) describedBy "The type of rotation" subOf rotate
    private val rotateYawStep = Setting("YawStep", YawStep.THRESHOLD) describedBy "How to yaw step" subOf rotate
    private val rotateYawStepThreshold = Setting("YawStepThreshold", 45f, 1f, 55f, 1f) describedBy "The yaw step threshold" subOf rotate visibleWhen { rotateYawStep.value == YawStep.THRESHOLD }


    /************************ Targeting Settings ************************/
    @Constant
    private val targeting = Setting("Targeting", true) describedBy "Settings for targeting"
    private val targetRange = Setting("Range", 10.0, 2.0, 15.0, 0.1) describedBy "The maximum range for targets to be away from you" subOf targeting
    private val targetSort = Setting("Sort", TargetPriority.HEALTH) describedBy "How to sort targets" subOf targeting
    private val targetPassives = Setting("Passives", false) describedBy "Target passives" subOf targeting
    private val targetHostiles = Setting("Hostiles", true) describedBy "Target hostile entities" subOf targeting
    private val targetPlayers = Setting("Players", true) describedBy "Target players" subOf targeting
    private val targetFriends = Setting("Friends", false) describedBy "Target friends" subOf targeting

    /************************ Placement Settings ************************/
    private val place = Setting("Place", true) describedBy "Place crystals"
    private val placeUpdated = Setting("Updated", false) describedBy "Allow you to place crystals in 1x1 holes [1.13+]" subOf place
    private val placeDelay = Setting("Delay", 0.0, 0.0, 100.0, 1.0) describedBy "The delay between placing crystals [milliseconds]" subOf place
    private val placeRange = Setting("Range", 5.0, 2.0, 6.0, 0.1) describedBy "The maximum distance you can place crystals away from you" subOf place
    private val placeRaytrace = Setting("Raytrace", Raytrace.HALF) describedBy "Checks if we can raycast to the block" subOf place
    private val placeMultiplace = Setting("Multiplace", false) describedBy "Excludes positions from searching that have a crystal on top of them" subOf place
    private val placeMinimum = Setting("Minimum", 4.0, 1.0, 20.0, 1.0) describedBy "The minimum damage a crystal has to do to an opponent" subOf place
    private val placeMaximum = Setting("Maximum", 8.0, 1.0, 20.0, 1.0) describedBy "The maximum damage a crystal can do to you" subOf place
    private val placeStrictFace = Setting("StrictFace", true) describedBy "Check that you can place on a correct place" subOf place
    private val placeSwitch = Setting("Switch", Switch.VANILLA) describedBy "How to switch to crystals" subOf place
    private val placePacket = Setting("Packet", true) describedBy "Place with a packet" subOf place
    private val placeSwing = Setting("Swing", true) describedBy "Swing your hand when you place" subOf place

    /************************ Explode Settings ************************/
    private val explode = Setting("Explode", true) describedBy "Explode crystals"
    private val explodeInstant = Setting("Instant", true) describedBy "Explode crystals when the crystal spawn packet is received" subOf explode
    private val explodeDelay = Setting("Delay", 0.0, 0.0, 100.0, 1.0) describedBy "The delay between exploding crystals [milliseconds]" subOf explode
    private val explodeRange = Setting("Range", 5.0, 2.0, 6.0, 0.1) describedBy "The maximum distance you can explode crystals away from you" subOf explode
    private val explodeWallRange = Setting("WallRange", 3.0, 1.0, 6.0, 0.1) describedBy "The maximum distance you can explode crystals away from you through walls" subOf explode
    private val explodeTicksExisted = Setting("TicksExisted", 0.0, 0.0, 5.0, 1.0) describedBy "The minimum amount of ticks a crystal has to have existed for before you can explode it" subOf explode
    private val explodeLimit = Setting("Limit", true) describedBy "Limit the amount of times you can attack a singular crystal" subOf explode
    private val explodeLimitThreshold = Setting("LimitThreshold", 3.0, 1.0, 5.0, 1.0) describedBy "The amount of times you can attack a singular crystal" subOf explode visibleWhen { explodeLimit.value }
    private val explodeSelf = Setting("Self", false) describedBy "Only explode crystals that you have placed" subOf explode
    private val explodeMinimum = Setting("Minimum", 4.0, 1.0, 20.0, 1.0) describedBy "The minimum damage a crystal has to do to an opponent" subOf explode
    private val explodeMaximum = Setting("Maximum", 8.0, 1.0, 20.0, 1.0) describedBy "The maximum damage a crystal can do to you" subOf explode
    private val explodeAntiSuicide = Setting("AntiSuicide", true) describedBy "Pause if the crystal you are about to explode will kill or pop you" subOf explode
    private val explodeStrictSprint = Setting("StrictSprint", true) describedBy "Force the player to stop sprinting when attacking" subOf explode
    private val explodeSync = Setting("Sync", Sync.ATTACK) describedBy "When to sync the crystal's dead status" subOf explode
    private val explodePacket = Setting("Packet", true) describedBy "Explode with a packet" subOf explode
    private val explodeSwing = Setting("Swing", true) describedBy "Swing your hand when you explode crystals" subOf explode

    /************************* Pause Settings *************************/
    private val pause = Setting("Pause", true) describedBy "Pause in certain situations"
    private val pauseEating = Setting("Eating", true) describedBy "Pause when eating" subOf pause
    private val pauseHealth = Setting("Health", true) describedBy "Pause when at a low health" subOf pause
    private val pauseHealthValue = Setting("HealthValue", 10.0, 1.0, 20.0, 1.0) describedBy "The health you need to be below in order to pause" subOf pause visibleWhen { pauseHealth.value }

    /************************ Render Settings ************************/
    private val render = Setting("Render", BoxRenderMode.BOTH) describedBy "Render the placement"
    private val renderOutlineWidth = Setting("OutlineWidth", 0.5f, 0.1f, 2f, 0.1f) describedBy "The width of the lines" subOf render
    private val renderColour = Setting("FillColour", Color(185, 19, 255, 130)) describedBy "The colour of the fill" subOf render
    private val renderOutlineColour = Setting("OutlineColour", Color(185, 19, 255)) subOf render
    private val renderDamageNametag = Setting("DamageNametag", true) describedBy "Render the damage nametag" subOf render

    // List of targets
    private val targets = arrayListOf<EntityLivingBase>()

    // Current crystal and placement
    var crystal: Crystal? = null
    private var placement: Placement? = null

    // Delay timers
    private val crystalTimer = Timer()
    private val placeTimer = Timer()

    // Attacked and placed crystals
    private val attackedCrystals: HashMap<Crystal, Int> = hashMapOf()
    private val placedCrystals = arrayListOf<BlockPos>()

    // The current state
    private var state = State.PLACING

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        // Reset placement and crystal
        placement = null
        crystal = null

        // Pause
        if (pause.value && (pauseEating.value && PlayerUtil.isPlayerConsuming || pauseHealth.value && EntityUtil.getEntityHealth(mc.player) <= pauseHealthValue.value)) {
            return
        }

        // Reset targets
        targets.clear()
        targets.addAll(findTargets())

        // Don't perform any calculations if there aren't any targets
        if (targets.isEmpty()) {
            return
        }

        // Perform actions
        when (order.value) {
            Order.PLACE_EXPLODE -> {
                // Place
                if (place.value && state == State.PLACING) {
                    // Find position
                    placement = findPlacement()

                    // Place the crystal if our timer has passed
                    if (placeTimer.hasMSPassed(placeDelay.value)) {
                        placeCrystal()

                        // If we want to spread actions across ticks
                        if (timing.value == Timing.SEQUENTIAL) {
                            return
                        }
                    }
                }

                // Explode
                if (explode.value && state == State.EXPLODING) {
                    // Find target crystal
                    crystal = findCrystal()

                    // Only explode if the time has passed
                    if (crystalTimer.hasMSPassed(explodeDelay.value)) {
                        explodeCrystal(crystal)
                    }
                }
            }

            Order.EXPLODE_PLACE -> {
                // Explode
                if (explode.value && state == State.EXPLODING) {
                    // Find target crystal
                    crystal = findCrystal()

                    // Only explode if the time has passed
                    if (crystalTimer.hasMSPassed(explodeDelay.value)) {
                        explodeCrystal(crystal)

                        // If we want to spread actions across ticks
                        if (timing.value == Timing.SEQUENTIAL) {
                            return
                        }
                    }
                }

                // Place
                if (place.value && state == State.PLACING) {
                    // Find position
                    placement = findPlacement()

                    // Place the crystal if our timer has passed
                    if (placeTimer.hasMSPassed(placeDelay.value)) {
                        placeCrystal()
                    }
                }
            }
        }
    }

    override fun onRender3D() {
        if (placement != null) {
            // Render placement
            RenderBuilder()
                .boundingBox(BlockUtil.getBlockBox(placement!!.position))
                .inner(renderColour.value)
                .outer(renderOutlineColour.value)
                .type(render.value)
                .start()
                .lineWidth(renderOutlineWidth.value)
                .blend(true)
                .depth(true)
                .texture(true)
                .build(false)

            // Render damage nametag
            if (renderDamageNametag.value) {
                RenderUtil.drawNametagText("[" + placement!!.targetDamage.toInt() + ", " + placement!!.selfDamage.toInt() + "]", Vec3d(placement!!.position.x + 0.5, placement!!.position.y + 0.5, placement!!.position.z + 0.5), Color.WHITE)
            }
        }
    }

    @Listener
    fun onEntityRemove(event: EntityRemoveFromWorldEvent) {
        // Remove entries from [attackedCrystals] if the key is the entity that was removed
        attackedCrystals.entries.removeIf { it.key.crystal == event.entity }

        // And the same for [placedCrystals]
        placedCrystals.removeIf { it == event.entity.position }
    }

    @Listener
    fun onPacketReceive(event: PacketEvent.PreReceive) {
        if (explode.value) {
            // Check it's a sound packet
            if (event.packet is SPacketSoundEffect && explodeSync.value == Sync.SOUND) {
                // Get packet
                val packet = event.packet

                // Check it's an explosion sound
                if (packet.sound == SoundEvents.ENTITY_GENERIC_EXPLODE && packet.category == SoundCategory.BLOCKS) {
                    // Iterate through loaded entities
                    for (entity in mc.world.loadedEntityList) {
                        // If the entity isn't an ender crystal, or it is dead, ignore
                        if (entity !is EntityEnderCrystal || entity.isDead) {
                            continue
                        }

                        // If the crystal is close to the explosion sound origin, set the crystals state to dead
                        if (entity.getDistance(packet.x, packet.y, packet.z) <= 6) {
                            entity.setDead()
                        }
                    }
                }
            }

            if (explode.value) {
                // Check it is a crystal spawn
                if (event.packet is SPacketSpawnObject && event.packet.type == 51 && explodeInstant.value) {
                    // Explode
                    explodeCrystal(event.packet.entityID)
                }
            }
        }
    }

    private fun findTargets(): ArrayList<EntityLivingBase> {
        val targets = arrayListOf<EntityLivingBase>()

        mc.world.loadedEntityList.forEach {
            // Ignore entities that we don't want to target, or are too far away, or aren't living
            if (!(it.isPassive() && targetPassives.value || it.isMonster() && targetHostiles.value || it is EntityOtherPlayerMP && targetPlayers.value) ||
                it.getDistance(mc.player) > targetRange.value || it !is EntityLivingBase) {

                return@forEach
            }

            // Ignore friends
            if (it is EntityOtherPlayerMP && Paragon.INSTANCE.friendManager.isFriend(it.name) && !targetFriends.value) {
                return@forEach
            }

            targets.add(it)
        }

        // Sort by [targetSort]'s algorithm
        targets.sortBy { targetSort.value.algorithm.apply(it) }

        return targets
    }

    private fun placeCrystal() {
        // Cancel if we don't have a placement, we want to place when we are holding crystals (and we aren't), or if we have no crystals in our hotbar
        if (placement == null || placeSwitch.value == Switch.HOLDING && !InventoryUtil.isHolding(Items.END_CRYSTAL) || InventoryUtil.getItemInHotbar(Items.END_CRYSTAL) <= -1) {
            state = State.EXPLODING
            return
        }

        // Current slot
        val previousSlot = mc.player.inventory.currentItem

        // The hand we are placing with
        val hand = if (mc.player.heldItemOffhand.item is ItemEndCrystal) EnumHand.OFF_HAND else EnumHand.MAIN_HAND

        // Switch to end crystal
        InventoryUtil.switchToItem(Items.END_CRYSTAL, placeSwitch.value == Switch.PACKET)

        if (rotate.value == RotateOn.PLACE || rotate.value == RotateOn.BOTH) {
            val rotation = RotationUtil.getRotationToVec3d(placement!!.facingVec)

            // Rotate if it isn't our current yaw
            if (Paragon.INSTANCE.rotationManager.serverRotation.x != rotation.x) {
                Paragon.INSTANCE.rotationManager.addRotation(
                    Rotation(
                        rotation.x,
                        rotation.y,
                        rotateType.value,
                        RotationPriority.HIGHEST,
                        when (rotateYawStep.value) {
                            YawStep.FULL -> 180f
                            YawStep.THRESHOLD -> rotateYawStepThreshold.value
                        }.toFloat()
                    )
                )
            }
        }

        // Sync current item
        (mc.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()

        if (placePacket.value) {
            // Send place packet
            mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(
                placement!!.position,
                placement!!.facing,
                hand,
                placement!!.facingVec.x.toFloat() - placement!!.position.x,
                placement!!.facingVec.y.toFloat() - placement!!.position.y,
                placement!!.facingVec.z.toFloat() - placement!!.position.z
            ))
        } else {
            // Vanilla place
            mc.playerController.processRightClickBlock(
                mc.player,
                mc.world,
                placement!!.position,
                placement!!.facing,
                placement!!.facingVec,
                hand
            )
        }

        if (placeSwing.value) {
            // Swing hand
            mc.player.swingArm(hand)
        }

        // Do not switch back if we want to keep the slot
        if (placeSwitch.value != Switch.KEEP) {
            InventoryUtil.switchToSlot(previousSlot, placeSwitch.value == Switch.PACKET)
        }

        // Add entity id to placed crystals
        if (!placedCrystals.contains(placement!!.position)) {
            placedCrystals.add(placement!!.position)
        }

        // Reset and change state
        placeTimer.reset()
        state = State.EXPLODING
    }

    private fun findPlacement(): Placement? {
        // Sorted placements
        val validPlacements: List<Placement> = getValidPlacements().sortedWith(Comparator.comparingDouble { placement -> placement.targetDamage.toDouble() }).reversed()

        return if (validPlacements.isEmpty()) null else validPlacements[0]
    }

    private fun getValidPlacements(): ArrayList<Placement> {
        val validPlacements = ArrayList<Placement>()

        // List of positions we can place on
        BlockUtil.getSphere(placeRange.value.toFloat(), true).filter { it.isPlaceable() }.forEach {
            // Exactly where the damage is caused from
            val damagePosition = Vec3d(it.x.toDouble() + 0.5, it.y + 1.0, it.z.toDouble() + 0.5)

            // The damage we have done to ourselves
            val selfDamage = BlockUtil.calculateExplosionDamage(damagePosition, mc.player)

            // The highest damage we can do to a target
            var highestTargetDamage = 0f

            targets.forEach { entity ->
                val damage = BlockUtil.calculateExplosionDamage(damagePosition, entity)

                if (damage > highestTargetDamage) {
                    highestTargetDamage = damage
                }
            }

            // Calculate target damage
            highestTargetDamage = calculateDamage(selfDamage, highestTargetDamage, mc.player.getDistanceSq(it).toFloat())

            // Ignore position if it isn't within our damage bounds
            if (!(highestTargetDamage > placeMinimum.value && selfDamage < placeMaximum.value)) {
                return@forEach
            }

            // Where we want to rotate to
            var rotationVec = Vec3d(it.x + 0.5, it.y + 0.5, it.z + 0.5)

            // Check we can raytrace
            if (placeRaytrace.value != Raytrace.NONE) {
                var finalResult: RayTraceResult? = null

                var x = 0.0

                while (x != 1.0) {
                    var y = 0.0

                    while (y != 1.0) {
                        var z = 0.0

                        while (z != 1.0) {
                            // Current vec
                            val vec = Vec3d(it.x + x, it.y + y, it.z + z)

                            // The result of raycasting there
                            val result = mc.world.rayTraceBlocks(
                                mc.player.getPositionEyes(1f),
                                vec,
                                false,
                                false,
                                true
                            )

                            // If it's a valid hit, set values
                            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK && result.blockPos == it) {
                                finalResult = result
                                rotationVec = vec
                                break
                            }

                            z += placeRaytrace.value.increase
                        }

                        y += placeRaytrace.value.increase
                    }

                    x += placeRaytrace.value.increase
                }

                // Ignore position if we can't raycast to it
                if (finalResult == null) {
                    return@forEach
                }
            }

            // The face we can place on
            val face = BlockUtil.getFacing(it) ?: if (placeStrictFace.value) return@forEach else EnumFacing.UP

            // Add to valid placements
            validPlacements.add(Placement(it, highestTargetDamage, selfDamage, face, rotationVec))
        }

        return validPlacements
    }

    private fun BlockPos.isPlaceable(): Boolean {
        val block = this.getBlockAtPos()
        val offsetOne = this.up().getBlockAtPos()
        val surrounding = this.up().getSurroundingBlocks()

        // We can only place on bedrock and obsidian
        if (!(block == Blocks.OBSIDIAN || block == Blocks.BEDROCK)) {
            return false
        }

        // We need one block of air
        if (offsetOne != Blocks.AIR) {
            return false
        }

        // We need at least one empty block
        val fullCount = surrounding.filter { it != Blocks.AIR }.size

        // If the position is fully surrounded by blocks, and we don't want to place in a 1x1 hole
        if (fullCount == 4 && !placeUpdated.value) {
            return false
        }

        // Check that there are no entities in on top of the position
        for (entity in mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(up()))) {
            // If the entity is dead, continue
            if (entity.isDead || !placeMultiplace.value && entity is EntityEnderCrystal) {
                continue
            }

            return false
        }

        return true
    }

    private fun explodeCrystal(crystal: Crystal?) {
        // Cancel if we don't have crystal to explode
        if (crystal == null) {
            state = State.PLACING
            return
        }

        // The sprint state
        var sprinting = false

        if (explodeStrictSprint.value) {
            // Set sprint state
            sprinting = mc.player.isSprinting || (mc.player as IEntityPlayerSP).hookGetServerSprintState()

            if (sprinting) {
                // Force stop sprinting
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING))
            }
        }

        if (rotate.value == RotateOn.EXPLODE || rotate.value == RotateOn.BOTH) {
            val rotation = RotationUtil.getRotationToVec3d(crystal.crystal.positionVector.addVector(0.0, 0.5, 0.0))

            // Rotate to crystal if it isn't our current rotation
            if (Paragon.INSTANCE.rotationManager.serverRotation.x != rotation.x) {
                Paragon.INSTANCE.rotationManager.addRotation(
                    Rotation(
                        rotation.x,
                        rotation.y,
                        rotateType.value,
                        RotationPriority.HIGHEST,

                        when (rotateYawStep.value) {
                            YawStep.FULL -> 180f
                            YawStep.THRESHOLD -> rotateYawStepThreshold.value
                        }.toFloat()
                    )
                )

                return
            }
        }

        if (explodePacket.value) {
            // Explode crystal with packet
            explodeCrystal(crystal.crystal.entityId)
        } else {
            // Vanilla attack
            mc.playerController.attackEntity(mc.player, crystal.crystal)
        }

        // Set dead
        if (explodeSync.value == Sync.ATTACK) {
            crystal.crystal.setDead()
        }

        // Swing hand
        if (explodeSwing.value) {
            mc.player.swingArm(EnumHand.MAIN_HAND)
        }

        if (sprinting) {
            // Start sprinting again
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING))
        }

        // Increase limit count
        attackedCrystals[crystal] = if (attackedCrystals.containsKey(crystal)) attackedCrystals[crystal]!! + 1 else 1

        // Reset timer and change state
        crystalTimer.reset()
        state = State.PLACING
    }

    private fun explodeCrystal(id: Int) {
        val attackPacket = CPacketUseEntity()

        // Change modifiers
        (attackPacket as ICPacketUseEntity).hookSetAction(CPacketUseEntity.Action.ATTACK)
        (attackPacket as ICPacketUseEntity).hookSetEntityID(id)

        mc.player.connection.sendPacket(attackPacket)
    }

    private fun findCrystal(): Crystal? {
        // Sorted crystals
        val validCrystals: List<Crystal> = getValidCrystals().sortedWith(Comparator.comparingDouble { crystal -> crystal.targetDamage.toDouble() }).reversed()

        return if (validCrystals.isEmpty()) null else validCrystals[0]
    }

    private fun getValidCrystals(): ArrayList<Crystal> {
        val validCrystals = arrayListOf<Crystal>()

        // Iterate through crystals in the world
        mc.world.loadedEntityList.filterIsInstance<EntityEnderCrystal>().forEach {
            // Get crystal valid status
            val crystal = isValid(it)

            // Add to [validCrystals] if it's a valid placement
            if (crystal.first.first && crystal.second != null) {
                validCrystals.add(Crystal(it, crystal.first.second, crystal.second!!.first, crystal.second!!.second))
            }
        }

        return validCrystals
    }

    private fun isValid(crystal: EntityEnderCrystal): Pair<Pair<Boolean, EntityLivingBase?>, Pair<Float, Float>?> {
        if (crystal.isDead) {
            return Pair(Pair(false, null), null)
        }

        // The distance to the crystal
        val range = crystal.getDistance(mc.player)

        // Ignore if we're past the attack limit or the crystal isn't old enough
        if (explodeLimit.value && attackedCrystals.any { (key, value) -> key.crystal == crystal && value > explodeLimitThreshold.value.toInt() } || crystal.ticksExisted < explodeTicksExisted.value) {
            return Pair(Pair(false, null), null)
        }

        // Ignore if we only want to explode our crystals
        if (explodeSelf.value && !placedCrystals.contains(crystal.position)) {
            return Pair(Pair(false, null), null)
        }

        // Whether we can't see the position
        val nonVisibility = mc.world.rayTraceBlocks(
            Vec3d(
                mc.player.posX,
                mc.player.posY + mc.player.getEyeHeight(),
                mc.player.posZ
            ),
            Vec3d(
                crystal.posX,
                crystal.posY + crystal.eyeHeight,
                crystal.posZ
            ),
            false,
            true,
            false
        ) != null

        if (nonVisibility) {
            // Check the range through a wall
            if (range > explodeWallRange.value) {
                return Pair(Pair(false, null), null)
            }
        } else {
            // Check the normal range
            if (range > explodeRange.value) {
                return Pair(Pair(false, null), null)
            }
        }

        // Damage to us
        val selfDamage = BlockUtil.calculateExplosionDamage(crystal.positionVector, mc.player)

        // Prevent killing / popping us
        if (selfDamage >= EntityUtil.getEntityHealth(mc.player) && explodeAntiSuicide.value) {
            return Pair(Pair(false, null), null)
        }

        var highestTargetDamage = 0f
        var target: EntityLivingBase? = null

        targets.forEach { entity ->
            val damage = BlockUtil.calculateExplosionDamage(crystal.positionVector, entity)

            if (damage > highestTargetDamage) {
                target = entity
                highestTargetDamage = damage
            }
        }

        // Calculate damage
        highestTargetDamage = calculateDamage(selfDamage, highestTargetDamage, mc.player.getDistance(crystal))

        // Ignore if the damage isn't within our damage bounds
        if (!(highestTargetDamage > explodeMinimum.value && selfDamage < explodeMaximum.value)) {
            return Pair(Pair(false, null), null)
        }

        return Pair(Pair(true, target), Pair(highestTargetDamage, selfDamage))
    }

    private fun calculateDamage(self: Float, target: Float, distance: Float): Float {
        return when (damage.value) {
            Damage.PLAIN -> target
            Damage.MINIMAX -> target - self
            Damage.UNIFORM -> target - self - distance
        }
    }

    private enum class Order {
        /**
         * Place crystals then explode them
         */
        PLACE_EXPLODE,

        /**
         * Explode crystals then explode them
         */
        EXPLODE_PLACE
    }

    private enum class Timing {
        /**
         * Perform all actions on the same tick
         */
        LINEAR,

        /**
         * Balance actions between ticks
         */
        SEQUENTIAL
    }

    private enum class State {
        /**
         * Exploding crystals
         */
        EXPLODING,

        /**
         * Placing crystals
         */
        PLACING
    }

    private enum class Damage {
        /**
         * Just the damage
         */
        PLAIN,

        /**
         * The difference between damage to self and damage to the target
         */
        MINIMAX,

        /**
         * Target damage minus self damage minus distance
         */
        UNIFORM
    }

    private enum class TargetPriority(val algorithm: Function<EntityLivingBase, Float>) {
        /**
         * Sort targets by distance away from you
         */
        DISTANCE({ entity -> entity.getDistance(mc.player) }),

        /**
         * Sort targets by their health
         */
        HEALTH({ entity -> EntityUtil.getEntityHealth(entity) })
    }

    private enum class Raytrace(val increase: Double) {
        /**
         * No raytrace
         */
        NONE(0.0),

        /**
         * Raytrace to middle
         */
        HALF(0.5),

        /**
         * Raytrace to each tenth of the block
         */
        TENTH(0.1),

        /**
         * Raytrace to each twentieth of the block
         */
        TWENTIETH(0.05),

        /**
         * Raytrace to each hundredth of the block
         */
        HUNDREDTH(0.01)
    }

    private enum class RotateOn {
        /**
         * Never rotate
         */
        NEVER,

        /**
         * Rotate when exploding crystals
         */
        EXPLODE,

        /**
         * Rotate when placing crystals
         */
        PLACE,

        /**
         * Rotate on both
         */
        BOTH
    }

    private enum class YawStep {
        /**
         * Rotate the entire way in one tick
         */
        FULL,

        /**
         * Rotate a given threshold
         */
        THRESHOLD
    }

    private enum class Switch {
        /**
         * Wait for us to be holding the item
         */
        HOLDING,

        /**
         * Normal switch + switch back
         */
        VANILLA,

        /**
         * Packet switch + switch back
         */
        PACKET,

        /**
         * Normal switch + keep slot
         */
        KEEP
    }

    private enum class Sync {
        /**
         * Sync crystal's alive status on attack
         */
        ATTACK,

        /**
         * Sync crystal's alive status on explosion sound
         */
        SOUND,

        /**
         * Do not sync
         */
        NEVER
    }

    data class Crystal(val crystal: EntityEnderCrystal, val target: EntityLivingBase?, val targetDamage: Float, val selfDamage: Float)
    private data class Placement(val position: BlockPos, val targetDamage: Float, val selfDamage: Float, val facing: EnumFacing, val facingVec: Vec3d)

}
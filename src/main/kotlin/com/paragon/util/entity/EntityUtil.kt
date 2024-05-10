package com.paragon.util.entity

import com.paragon.util.mc
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityAgeable
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.EnumCreatureType
import net.minecraft.entity.monster.EntityEnderman
import net.minecraft.entity.monster.EntityIronGolem
import net.minecraft.entity.monster.EntityPigZombie
import net.minecraft.entity.monster.EntitySpider
import net.minecraft.entity.passive.EntityAmbientCreature
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityWolf
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.TextFormatting


object EntityUtil {

    /**
     * Gets the interpolated position of a given entity
     *
     * @param entityIn The given entity
     * @return The interpolated position
     */
    fun getInterpolatedPosition(entityIn: Entity): Vec3d {
        return Vec3d(entityIn.lastTickPosX, entityIn.lastTickPosY, entityIn.lastTickPosZ).add(
            getInterpolatedAmount(
                entityIn, mc.renderPartialTicks
            )
        )
    }

    /**
     * Gets the interpolated amount of the entity
     *
     * @param entity       The entity in
     * @param partialTicks The render partial ticks
     * @return The interpolated amount
     */
    private fun getInterpolatedAmount(entity: Entity, partialTicks: Float): Vec3d {
        return Vec3d(
            (entity.posX - entity.lastTickPosX) * partialTicks, (entity.posY - entity.lastTickPosY) * partialTicks, (entity.posZ - entity.lastTickPosZ) * partialTicks
        )
    }

    /**
     * Gets the text formatting colour based on an entity's health
     *
     * @param entity The entity
     * @return The colour of the health
     */
    fun getTextColourFromEntityHealth(entity: EntityLivingBase) = when {
        getEntityHealth(entity) > 20 -> TextFormatting.YELLOW
        entity.health <= 20 && entity.health > 15 -> TextFormatting.GREEN
        entity.health <= 15 && entity.health > 10 -> TextFormatting.GOLD
        entity.health <= 10 && entity.health > 5 -> TextFormatting.RED
        entity.health <= 5 -> TextFormatting.DARK_RED
        else -> TextFormatting.GRAY
    }

    /**
     * Gets the bounding box of an entity
     *
     * @param entity The entity
     * @return The bounding box of the entity
     */
    fun getEntityBox(entity: Entity) = AxisAlignedBB(
        entity.entityBoundingBox.minX - entity.posX + (entity.posX - mc.renderManager.viewerPosX),
        entity.entityBoundingBox.minY - entity.posY + (entity.posY - mc.renderManager.viewerPosY),
        entity.entityBoundingBox.minZ - entity.posZ + (entity.posZ - mc.renderManager.viewerPosZ),
        entity.entityBoundingBox.maxX - entity.posX + (entity.posX - mc.renderManager.viewerPosX),
        entity.entityBoundingBox.maxY - entity.posY + (entity.posY - mc.renderManager.viewerPosY),
        entity.entityBoundingBox.maxZ - entity.posZ + (entity.posZ - mc.renderManager.viewerPosZ)
    )

    /**
     * Checks if a player's distance from us is further than the given maximum range
     *
     * @param maximumRange The maximum range they are allowed in
     * @return If the player is too far away from us
     */
    @JvmStatic
    fun Entity.isTooFarAwayFromSelf(maximumRange: Double) = this.getDistance(mc.player) > maximumRange

    @JvmStatic
    fun Entity.isEntityAllowed(players: Boolean, mobs: Boolean, passives: Boolean) = when {
        this is EntityPlayer && players && this !== mc.player -> true
        this.isMonster() && mobs -> true
        else -> this.isPassive() && passives
    }

    /**
     * Checks whether an [Entity] is a monster or not.
     */
    @JvmStatic
    fun Entity.isMonster() = this.isCreatureType(
        EnumCreatureType.MONSTER,
        false
    ) && !(this is EntityPigZombie || this is EntityWolf || this is EntityEnderman) || this is EntitySpider

    /**
     * Checks whether an [Entity] is passive or not.
     */
    @JvmStatic
    fun Entity.isPassive() = when (this) {
        is EntityWolf -> !this.isAngry
        is EntityIronGolem -> (this as EntityLivingBase).revengeTarget == null
        else -> this is EntityAgeable || this is EntityAmbientCreature || this is EntitySquid
    }

    /**
     * @return the total health of an [EntityLivingBase].
     */
    @JvmStatic
    fun getEntityHealth(entityLivingBase: EntityLivingBase): Float {
        return entityLivingBase.health + entityLivingBase.absorptionAmount
    }

}
package com.paragon.util.combat

import com.paragon.util.mc
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.MobEffects
import net.minecraft.util.CombatRules
import net.minecraft.util.DamageSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.Explosion
import kotlin.math.max


/**
 * @author Surge
 * @since 10/08/2022
 */
object CrystalUtil {

    /**
     * Calculates the damage a crystal if exploded would do to an entity.
     *
     * @return the result of the calculation
     */
    @JvmStatic
    fun EntityEnderCrystal.getDamageToEntity(entity: EntityLivingBase): Float {
        return calculateDamage(Vec3d(this.posX, this.posY, this.posZ), entity)
    }

    @JvmStatic
    fun BlockPos.getCrystalDamage(entity: EntityLivingBase): Float {
        return calculateDamage(Vec3d(this.x.toDouble() + 0.5, this.y.toDouble() + 1, this.z.toDouble() + 0.5), entity)
    }

    /**
     * Calculates the explosion damage based on a Vec3D
     *
     * @param vec The vector to calculate damage from
     * @param entity The target
     * @return The damage done to the target
     */
    @JvmStatic
    fun calculateDamage(vec: Vec3d, entity: EntityLivingBase): Float {
        if (entity.isDead) {
            return 0f
        }

        val doubleExplosionSize = 12.0f
        val v = (1.0 - entity.getDistance(
            vec.x,
            vec.y,
            vec.z
        ) / doubleExplosionSize.toDouble()) * entity.world.getBlockDensity(
            Vec3d(vec.x, vec.y, vec.z),
            entity.entityBoundingBox
        ).toDouble()
        val diff = mc.world.difficulty.difficultyId.toFloat()

        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        return getBlastReduction(
            entity,
            ((v * v + v) / 2f * 7f * doubleExplosionSize.toDouble() + 1f).toFloat() * if (diff == 0f) 0f else if (diff == 2f) 1f else if (diff == 1f) 0.5f else 1.5f,
            Explosion(mc.world, null, vec.x, vec.y, vec.z, 6f, false, true)
        )
    }

    /**
     * Gets the blast reduction
     *
     * @param entity The entity to calculate damage for
     * @param damage The original damage
     * @param explosion The explosion
     * @return The blast reduction
     */
    @JvmStatic
    private fun getBlastReduction(entity: EntityLivingBase, damage: Float, explosion: Explosion): Float {
        var dmg = damage

        dmg = CombatRules.getDamageAfterAbsorb(
            dmg,
            entity.totalArmorValue.toFloat(),
            entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).attributeValue.toFloat()
        )

        runCatching {
            dmg *= 1.0f - MathHelper.clamp(
                EnchantmentHelper.getEnchantmentModifierDamage(
                    entity.armorInventoryList,
                    DamageSource.causeExplosionDamage(explosion)
                ),
                0,
                20
            ) / 25.0F

            if (entity.isPotionActive(MobEffects.WEAKNESS)) {
                dmg -= dmg / 4
            }
        }

        return max(dmg, 0.0F)
    }

}
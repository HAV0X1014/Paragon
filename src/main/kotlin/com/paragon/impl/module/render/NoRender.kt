package com.paragon.impl.module.render

import com.paragon.impl.event.render.entity.HurtcamEvent
import com.paragon.impl.event.render.entity.RenderEatingEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.world.ParticleSpawnEvent
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.client.particle.*
import net.minecraft.entity.passive.EntityBat
import net.minecraft.init.SoundEvents
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.client.event.RenderBlockOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object NoRender : Module("NoRender", Category.RENDER, "Cancels rendering certain things") {

    private val fire = Setting(
        "Fire", true
    ) describedBy "Cancel rendering the fire overlay"

    private val water = Setting(
        "Water", true
    ) describedBy "Cancel rendering the water overlay"

    private val bossInfo = Setting(
        "BossInfo", true
    ) describedBy "Cancel rendering the boss info overlay"

    private val potions = Setting(
        "PotionIcons", false
    ) describedBy "Cancel rendering the potion icons"

    private val portal = Setting(
        "Portal", true
    ) describedBy "Cancel rendering the portal effect"

    private val explosion = Setting(
        "Explosions", true
    ) describedBy "Cancel explosion particles"

    private val firework = Setting(
        "Fireworks", true
    ) describedBy "Cancel firework particles"

    private val totem = Setting(
        "Totems", true
    ) describedBy "Cancel totem pop particles"

    private val bats = Setting(
        "Bats", true
    ) describedBy "Cancel rendering bats"

    private val eatingAnimation = Setting(
        "EatingAnimation", false
    ) describedBy "Stops rendering the eating animation"

    private val hurtcam = Setting(
        "Hurtcam", true
    ) describedBy "Cancel rendering the hurtcam"

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Pre) {
        if (mc.anyNull) {
            return
        }

        if (bossInfo.value && event.type == RenderGameOverlayEvent.ElementType.BOSSINFO) {
            event.isCanceled = true
        }

        if (potions.value && event.type == RenderGameOverlayEvent.ElementType.POTION_ICONS) {
            event.isCanceled = true
        }

        if (portal.value && event.type == RenderGameOverlayEvent.ElementType.PORTAL) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onRenderLivingEntity(event: RenderLivingEvent.Pre<*>) {
        if (mc.anyNull) {
            return
        }
        if (bats.value && event.entity is EntityBat) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onPlaySound(event: PlaySoundAtEntityEvent) {
        if (mc.anyNull) {
            return
        }

        if (bats.value && event.sound == SoundEvents.ENTITY_BAT_AMBIENT || event.sound == SoundEvents.ENTITY_BAT_DEATH || event.sound == SoundEvents.ENTITY_BAT_HURT || event.sound == SoundEvents.ENTITY_BAT_LOOP || event.sound == SoundEvents.ENTITY_BAT_TAKEOFF) {
            event.volume = 0.0f
            event.pitch = 0.0f
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onBlockOverlay(event: RenderBlockOverlayEvent) {
        if (fire.value && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.isCanceled = true
        }
        if (water.value && event.overlayType == RenderBlockOverlayEvent.OverlayType.WATER) {
            event.isCanceled = true
        }
    }

    @Listener
    fun onParticleSpawn(event: ParticleSpawnEvent) {
        when (event.particle) {
            is ParticleExplosion, is ParticleExplosionHuge, is ParticleExplosionLarge -> if (explosion.value) event.cancel()
            is ParticleFirework.Overlay, is ParticleFirework.Spark, is ParticleFirework.Starter -> if (firework.value) event.cancel()
            is ParticleTotem -> if (totem.value) event.cancel()
        }
    }

    @Listener
    fun onRenderEating(event: RenderEatingEvent) {
        if (eatingAnimation.value) {
            event.cancel()
        }
    }

    @Listener
    fun onHurtcam(event: HurtcamEvent) {
        if (hurtcam.value) {
            event.cancel()
        }
    }

}
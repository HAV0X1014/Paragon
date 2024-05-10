package com.paragon.impl.event.render.world

import com.paragon.bus.event.CancellableEvent
import net.minecraft.client.particle.Particle

class ParticleSpawnEvent(var particle: Particle) : CancellableEvent()
package com.paragon.mixins.render.world;

import com.paragon.Paragon;
import com.paragon.impl.event.render.world.ParticleSpawnEvent;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class MixinParticleManager {

    @Inject(method = "addEffect", at = @At(value = "HEAD"), cancellable = true)
    public void addEffect(Particle particle, CallbackInfo info) {
        ParticleSpawnEvent event = new ParticleSpawnEvent(particle);
        Paragon.INSTANCE.getEventBus().post(event);
        if (event.isCancelled()) info.cancel();
    }

}

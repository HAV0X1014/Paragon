package com.paragon.mixins.world;

import com.paragon.Paragon;
import com.paragon.impl.event.world.entity.EntityRemoveFromWorldEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class MixinWorld {

    @Inject(method = "removeEntity", at = @At("HEAD"))
    public void hookRemoveEntity(Entity entity, CallbackInfo ci) {
        EntityRemoveFromWorldEvent entityRemoveFromWorldEvent = new EntityRemoveFromWorldEvent(entity);
        Paragon.INSTANCE.getEventBus().post(entityRemoveFromWorldEvent);
    }

}

package com.paragon.mixins.player;

import com.paragon.Paragon;
import com.paragon.impl.event.player.TravelEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {

    public MixinEntityPlayer(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void hookTravel(float strafe, float vertical, float forward, CallbackInfo info) {
        TravelEvent travelEvent = new TravelEvent();
        Paragon.INSTANCE.getEventBus().post(travelEvent);

        if (travelEvent.isCancelled()) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            info.cancel();
        }
    }

}

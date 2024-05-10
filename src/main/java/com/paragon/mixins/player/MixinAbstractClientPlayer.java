package com.paragon.mixins.player;

import com.mojang.authlib.GameProfile;
import com.paragon.Paragon;
import com.paragon.impl.command.impl.CopySkinCommand;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer {

    public MixinAbstractClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void hookGetLocationCape(CallbackInfoReturnable<ResourceLocation> cir) {
        if (Paragon.INSTANCE.getCapeManager().isCaped(getName())) {
            cir.setReturnValue(new ResourceLocation(Paragon.MOD_ID, Paragon.INSTANCE.getCapeManager().getCape(getName()).getPath()));
        }
    }

    @Inject(method = "getLocationSkin*", at = @At("HEAD"), cancellable = true)
    public void hookGetLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        if (CopySkinCommand.INSTANCE.getSkin() != null) {
            cir.setReturnValue(CopySkinCommand.INSTANCE.getSkin());
        }
    }

}

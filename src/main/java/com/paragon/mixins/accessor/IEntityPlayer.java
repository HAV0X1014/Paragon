package com.paragon.mixins.accessor;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author SooStrator1136
 */
@Mixin(EntityPlayer.class)
public interface IEntityPlayer {

    @Accessor("gameProfile")
    GameProfile hookGetGameProfile();

}

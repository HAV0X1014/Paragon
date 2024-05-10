package com.paragon.mixins.accessor;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author Surge
 * @since 15/11/2022
 */
@Mixin(EntityPlayerSP.class)
public interface IEntityPlayerSP {

    @Accessor("serverSprintState")
    boolean hookGetServerSprintState();

}

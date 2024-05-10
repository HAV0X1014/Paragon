package com.paragon.mixins.accessor;

import net.minecraft.client.gui.MapItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author SooStrator1136
 */
@Mixin(MapItemRenderer.class)
public interface IMapItemRenderer {

    @Accessor("loadedMaps")
    Map<String, Object> hookGetLoadedMaps();

}

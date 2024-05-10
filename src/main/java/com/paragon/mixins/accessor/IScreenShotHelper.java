package com.paragon.mixins.accessor;

import net.minecraft.util.ScreenShotHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.File;

/**
 * @author SooStrator1136
 */
@Mixin(ScreenShotHelper.class)
public interface IScreenShotHelper {

    @Invoker("getTimestampedPNGFileForDirectory")
    public static File getTimestampedPNGFileForDirectory(File file1) {
        throw new AssertionError();
    }

}

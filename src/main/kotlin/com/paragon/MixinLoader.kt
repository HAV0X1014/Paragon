package com.paragon

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import org.spongepowered.asm.launch.MixinBootstrap
import org.spongepowered.asm.mixin.MixinEnvironment
import org.spongepowered.asm.mixin.Mixins

@IFMLLoadingPlugin.Name("ParagonMixinLoader")
@IFMLLoadingPlugin.MCVersion("1.12.2")
class MixinLoader : IFMLLoadingPlugin {

    init {
        MixinBootstrap.init()
        Mixins.addConfiguration("mixins.paragon.json")
        MixinEnvironment.getDefaultEnvironment().obfuscationContext = "searge"

        Paragon.INSTANCE.logger.info("Mixins loaded with context '${MixinEnvironment.getDefaultEnvironment().obfuscationContext}'")
    }

    override fun getASMTransformerClass(): Array<String> = emptyArray()
    override fun getModContainerClass(): String? = null
    override fun getSetupClass(): String? = null
    override fun injectData(data: Map<String, Any>) = Unit
    override fun getAccessTransformerClass(): String? = null
}

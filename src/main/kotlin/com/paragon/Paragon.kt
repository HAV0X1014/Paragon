package com.paragon

import com.paragon.bus.EventBus
import com.paragon.impl.event.EventFactory
import com.paragon.impl.managers.*
import com.paragon.impl.ui.configuration.ConfigurationGUI
import com.paragon.impl.ui.configuration.camper.CamperCheatGUI
import com.paragon.impl.ui.configuration.discord.DiscordGUI
import com.paragon.impl.ui.configuration.panel.PanelGUI
import com.paragon.impl.ui.configuration.phobos.PhobosGUI
import com.paragon.impl.ui.configuration.retrowindows.Windows98
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.Minecraft
import net.minecraftforge.common.ForgeVersion
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.Display
import java.awt.Desktop
import java.net.URI
import javax.swing.JOptionPane

@Mod(name = Paragon.NAME, modid = Paragon.MOD_ID, version = Paragon.VERSION)
class Paragon {

    @EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        if (ForgeVersion.buildVersion < 2860) {
            JOptionPane.showMessageDialog(
                null, "Forge version is too old. Paragon requires Forge to be at least build 2860.", "Outdated Forge!", JOptionPane.ERROR_MESSAGE
            )

            Desktop.getDesktop().browse(
                URI("https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html")
            )

            Minecraft.getMinecraft().shutdown()

            // When trying to exit throws an exception lmao
            Display.destroy()

            return
        }

        eventParser = EventFactory()
        FontUtil.init()
    }

    @EventHandler
    fun init(event: FMLInitializationEvent?) {
        logger.info("Starting Paragon $VERSION initialisation")

        storageManager = StorageManager()
        logger.info("StorageManager initialised")

        // Module / Commands

        moduleManager = ModuleManager()
        logger.info("ModuleManager initialised")

        commandManager = CommandManager()
        logger.info("CommandManager initialised")

        // Misc client stuff

        altManager = AltManager()
        logger.info("AltManager initialised")

        capeManager = CapeManager()

        notificationManager = NotificationManager()
        logger.info("NotificationManager initialised")

        friendManager = FriendManager()
        logger.info("SocialManager initialised")

        // Event / Ingame stuff

        popManager = PopManager()
        logger.info("PopManager initialised")

        rotationManager = RotationManager()
        logger.info("RotationManager initialised")

        tpsManager = TPSManager()
        lagCompensator = LagCompensator()
        logger.info("TPS Utilities initialised")

        // Load

        storageManager.loadModules("current")
        logger.info("Modules Loaded")

        storageManager.loadSocial()
        logger.info("Social Loaded")

        storageManager.loadAlts()
        logger.info("Alts Loaded")

        storageManager.loadOther()
        logger.info("Other Loaded")

        // GUIs

        panelGUI = PanelGUI()
        logger.info("PanelGUI Initialised")

        windows98GUI = Windows98()
        logger.info("Windows98 GUI Initialised")

        phobosGUI = PhobosGUI()
        logger.info("Phobos GUI Initialised")

        discordGUI = DiscordGUI()
        logger.info("Discord GUI Initialised")

        camperCheatGUI = CamperCheatGUI()
        logger.info("CamperCheat GUI Initialised")

        configurationGUI = ConfigurationGUI()
        logger.info("Configuration GUI Initialised")

        logger.info("Paragon $VERSION Initialised Successfully")
    }

    companion object {
        const val NAME = "Paragon"
        const val MOD_ID = "paragon"
        const val VERSION = "1.1.0"

        @JvmField
        @Mod.Instance
        var INSTANCE = Paragon()
    }

    val eventBus = EventBus()

    // Client stuff
    var logger: Logger = LogManager.getLogger("paragon")
        private set

    val presenceManager = DiscordPresenceManager()

    // Managers
    lateinit var storageManager: StorageManager
        private set

    lateinit var moduleManager: ModuleManager
        private set

    lateinit var commandManager: CommandManager
        private set

    private lateinit var eventParser: EventFactory

    lateinit var popManager: PopManager
        private set

    lateinit var rotationManager: RotationManager
        private set

    lateinit var friendManager: FriendManager
        private set

    lateinit var altManager: AltManager
        private set

    lateinit var notificationManager: NotificationManager
        private set

    lateinit var capeManager: CapeManager
        private set

    lateinit var tpsManager: TPSManager
        private set

    lateinit var lagCompensator: LagCompensator
        private set

    // GUIs
    lateinit var panelGUI: PanelGUI
        private set

    lateinit var windows98GUI: Windows98
        private set

    lateinit var phobosGUI: PhobosGUI
        private set

    lateinit var discordGUI: DiscordGUI
        private set

    lateinit var camperCheatGUI: CamperCheatGUI
        private set

    lateinit var configurationGUI: ConfigurationGUI
        private set

}
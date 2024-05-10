package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.event.render.gui.GetChatLineCountEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.util.text.TextComponentString
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.random.Random


/**
 * @author Surge
 */
@Aliases(["Spammer", "Cryptic", "Encrypt"])
object ChatModifications : Module("ChatModifications", Category.MISC, "Changes the way you send messages") {

    private val infinite = Setting("Infinite", true) describedBy "Doesn't limit the amount of chat messages shown"
    private val coloured = Setting("Coloured", false) describedBy "Adds a '>' before the message"
    private val suffix = Setting("Suffix", true) describedBy "Adds a Paragon suffix to the end of the message"

    private val spammer = Setting("Spammer", false) describedBy "Sends messages in chat (defined in paragon/spammer.txt)"
    private val spammerMode = Setting("Mode", SpammerMode.DOWN) describedBy "How to select messages from spammer.txt" subOf spammer
    private val spammerLimit = Setting("Limit", 100.0, -1.0, 300.0, 1.0) describedBy "The maximum amount of characters per message. any message longer than this will be split into two." subOf spammer
    private val spammerDelay = Setting("Delay", 10.0, 1.0, 120.0, 1.0) describedBy "Delay between messages (in seconds)" subOf spammer

    val cryptic = Setting("Cryptic", false) describedBy "Encrypts and decrypts messages"
    val requirePrefix = Setting("RequirePrefix", true) describedBy "Require a prefix ('crypt <message>') to be used before the message" subOf cryptic
    private val cancel = Setting("Cancel", false) describedBy "Cancel showing the original chat message" subOf cryptic

    private var lines: Array<String> = arrayOf()
    private var lastMS: Long = 0L
    private var messageIndex = 0
    private val messageQueue = ConcurrentLinkedQueue<String>()

    const val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private val suffixes: Array<String> = arrayOf(
        "yLdhiv",
        "pTu5mw",
        "9Ebk8f",
        "Hd5agt",
        "FNjlYy",
        "SmI5KV",
        "NhDvvP",
        "ZKMxOp",
        "wkc9Ly",
        "D0BoQt",
        "TyUOUE",
        "kfMs1F",
        "gGNFhF",
        "nJrfKn",
        "zgMqiN",
        "EAuPbt",
        "H8bjLg",
        "Ryy5UJ",
        "5tzUtY",
        "paA9e3",
        "YkecGe",
        "8yrqHF",
        "vrbu7W",
        "vZg1AG",
        "9WI9c2",
        "OVfCyP",
        "w91h1d",
        "FWjhBe",
        "AtNi8R",
        "rIYvCU",
        "vZCy5e",
        "dIwW0s",
        "MpPcsX",
        "wy0J6Q",
        "YGG4Ku",
        "lPwaiN",
        "CNjfvG",
        "6hiCT6",
        "d5Yuv1",
        "b1KNMC",
        "mtb4Ie",
        "5SLfbC",
        "e0Enlj",
        "HQyG25",
        "QhJEqy",
        "2ppBSV",
        "f5rHgK",
        "FzQh0h",
        "lL7esI",
        "F0nkYp",
        "X93GYo",
        "ixCAsZ",
        "SCGvQE",
        "MEVIO2",
        "AACBkP",
        "v6ZghF",
        "cdVQf3",
        "bkC9OE",
        "XKNrnt",
        "xWP6CA",
        "l2R4TP",
        "7vBg9i",
        "5d8t20",
        "E6Q6w9",
        "5zuONf",
        "C6Hd6r",
        "jBQmw3",
        "Q9Uytu",
        "xLiFXR",
        "qdf3eC",
        "OJ9zxE",
        "2vugXy",
        "0Zk61E",
        "7rijaU",
        "TwCFjE",
        "mnFEqF",
        "4QkZdp",
        "8Ui6Fz",
        "seEuG9",
        "TCHrTU",
        "ec5iU9",
        "5Tta37",
        "ExgT1T",
        "P0GmzK",
        "JTA8lg",
        "cOKwPu",
        "D59VyI",
        "9XIHeV",
        "CIf1gn",
        "jMvMWZ",
        "kTEaaC",
        "u9drvb",
        "fgGPBQ",
        "HAYzcD",
        "ruKvpT",
        "vNMCO1",
        "PHiRo2",
        "j9LOEF",
        "QOMIK3",
        "bUq4p1"
    )

    override fun onEnable() {
        loadLines()
    }

    override fun onTick() {
        if (mc.anyNull || !spammer.value) {
            return
        }

        if (System.currentTimeMillis() - lastMS > spammerDelay.value * 1000) {
            lastMS = System.currentTimeMillis()

            if (lines.isEmpty()) {
                loadLines()
            }

            when (spammerMode.value) {
                SpammerMode.RANDOM -> sendMessage(lines[(Math.random() * lines.size).toInt()])

                SpammerMode.DOWN -> {
                    if (messageIndex > lines.size - 1) {
                        messageIndex = 0
                    }

                    sendMessage(lines[messageIndex])

                    messageIndex++
                }
            }

            if (!messageQueue.isEmpty()) {
                mc.player.sendChatMessage(messageQueue.poll())
            }
        }
    }

    @Listener
    fun onGetChatLineCount(event: GetChatLineCountEvent) {
        if (infinite.value) {
            event.size = -Int.MAX_VALUE
        }
    }

    @Listener
    fun onSettingUpdate(event: SettingUpdateEvent) {
        if (event.setting == spammerMode) {
            loadLines()
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatEvent) {
        if (event.message.startsWith("/")) {
            return
        }

        if (!Paragon.INSTANCE.commandManager.startsWithPrefix(event.message)) {
            if (coloured.value) {
                event.message = "> " + event.message
            }

            if (suffix.value) {
                event.message = event.message + " | Paragon"
            }
        }

        if (cryptic.value) {
            if (requirePrefix.value && !event.message.startsWith("crypt ") || Paragon.INSTANCE.commandManager.startsWithPrefix(event.message)) {
                return
            }

            val rand = Random.nextInt(8) + 1
            val suffix = Random.nextInt(suffixes.size)

            event.message = encrypt(event.message.replace("crypt ", ""), rand) + rand + suffixes[suffix]
        }
    }

    @SubscribeEvent
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (cryptic.value) {
            val message: String = event.message.unformattedText

            // Ends with keyboard mash
            if (suffixes.contains(message.substring(message.length - 6, message.length))) {
                val player = event.message.unformattedText.substring(0, event.message.unformattedText.indexOf(">"))

                val rawMessage = message.substring(message.indexOf('>'), message.length - 7)
                val decoded: String = player + decrypt(rawMessage, message.substring(message.length - 7, message.length - 6).toInt())

                if (player.equals(mc.player.name, true)) {
                    return
                }

                if (cancel.value) {
                    event.isCanceled = true
                }

                mc.ingameGUI.chatGUI.printChatMessage(TextComponentString("$decoded [Decrypted by Paragon]"))
            }
        }
    }

    private fun loadLines() {
        val file = File("paragon/spammer.txt")

        if (!file.exists()) {
            file.createNewFile()
            file.writeText("spammed by paragon client, llc")
        }

        lines = file.readLines().toTypedArray()
        messageIndex = 0
    }

    private fun encrypt(message: String, shiftKey: Int): String? {
        var cipherText: String? = ""

        for (element in message.lowercase(Locale.getDefault())) {
            cipherText += if (alpha.contains(element)) {
                val charPosition: Int = alpha.indexOf(element)
                val keyVal = (shiftKey + charPosition) % alpha.length
                val replaceVal: Char = alpha[keyVal]

                replaceVal
            }
            else {
                element
            }
        }

        return cipherText
    }

    private fun decrypt(cipherText: String, shiftKey: Int): String? {
        var message: String? = ""

        for (element in cipherText.lowercase(Locale.getDefault())) {
            if (alpha.contains(element)) {
                val charPosition: Int = alpha.indexOf(element)
                var keyVal = (charPosition - shiftKey) % alpha.length

                if (keyVal < 0) {
                    keyVal += alpha.length
                }

                val replaceVal: Char = alpha[keyVal]
                message += replaceVal
            }
            else {
                message += element
            }
        }

        return message
    }

    private fun sendMessage(message: String) {
        if (spammerLimit.value >= 0) {
            val length: Int = message.length

            var i = 0
            while (i < length) {
                messageQueue.add(message.substring(i, length.coerceAtMost(i + spammerLimit.value.toInt())))
                i += spammerLimit.value.toInt()
            }
        } else {
            messageQueue.add(message)
        }
    }

    private enum class SpammerMode {
        RANDOM,
        DOWN
    }

}
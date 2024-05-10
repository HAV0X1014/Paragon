package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.managers.alt.Alt
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Bind.Device
import org.apache.commons.io.FileUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.awt.Color
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * @author SooStrator1136
 */
class StorageManager {

    private val mainFolder = File("paragon")
    private val configFolder = File("paragon${File.separator}configs${File.separator}")
    private val socialFolder = File("paragon${File.separator}social")

    @Throws(IOException::class, JSONException::class)
    private fun getJSON(file: File) = if (!Files.exists(file.toPath())) {
        null
    } else JSONObject(FileUtils.readFileToString(file, StandardCharsets.UTF_8))

    fun saveModules(configName: String) {
        // Create configs folder if it doesn't already exist
        if (!configFolder.exists()) {
            configFolder.mkdirs()
        }

        val configFile = File("paragon${File.separator}configs${File.separator}$configName.json")

        val json = JSONObject()

        Paragon.INSTANCE.moduleManager.modules.forEach {
            try {
                val settings = JSONObject()

                settings.put("enabled", it.isEnabled)

                if (it is HUDModule) {
                    settings.put("x", it.x)
                    settings.put("y", it.y)
                }

                it.settings.forEach { setting ->
                    try {
                        when (setting.value) {
                            is Color -> {
                                val color = setting.value as Color

                                settings.put(
                                    setting.name,
                                    color.red.toString() + ":" +
                                            color.green + ":" +
                                            color.blue + ":" +
                                            color.alpha + ":" +
                                            setting.isRainbow + ":" +
                                            setting.rainbowSpeed + ":" +
                                            setting.rainbowSaturation + ":" +
                                            setting.isSync
                                )
                            }

                            is Bind -> {
                                val bind = setting.value as Bind
                                settings.put(
                                    setting.name, bind.buttonCode.toString() + ":" + bind.device
                                )
                            }

                            else -> settings.put(setting.name, setting.value)
                        }

                        if (setting.subsettings.isNotEmpty()) {
                            for (subSetting in setting.subsettings) {
                                val subSettingName = subSetting.parentSetting?.name + " " + subSetting.name
                                when (subSetting.value) {
                                    is Color -> {
                                        val color = subSetting.value as Color

                                        settings.put(
                                            subSettingName,
                                            color.red.toString() + ":" +
                                                    color.green + ":" +
                                                    color.blue + ":" +
                                                    color.alpha + ":" +
                                                    subSetting.isRainbow + ":" +
                                                    subSetting.rainbowSpeed + ":" +
                                                    subSetting.rainbowSaturation + ":" +
                                                    subSetting.isSync
                                        )
                                    }

                                    is Bind -> {
                                        val bind = subSetting.value as Bind
                                        settings.put(
                                            subSettingName, bind.buttonCode.toString() + ":" + bind.device
                                        )
                                    }

                                    else -> settings.put(subSettingName, subSetting.value)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                json.put(it.name, settings)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        try {
            // Create file writer instance
            val fileWriter = FileWriter(configFile)

            // Write with indentation factor of 4
            fileWriter.write(json.toString(4))

            // Flush and close
            fileWriter.flush()
            fileWriter.close()
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun loadModules(configName: String) {
        // Create configs folder if it doesn't already exist
        if (!configFolder.exists()) {
            configFolder.mkdirs()
        }

        val configFile = File("paragon${File.separator}configs${File.separator}$configName.json")

        val json = getJSON(configFile) ?: return

        Paragon.INSTANCE.moduleManager.modules.forEach {
            try {
                val settings = json.getJSONObject(it.name)

                if (settings.has("x") && settings.has("y") && it is HUDModule) {
                    it.x = settings.getInt("x").toFloat()
                    it.y = settings.getInt("y").toFloat()
                }

                fun loadSetting(setting: Setting<*>, isSub: Boolean) {
                    runCatching {
                        val settingName = if (isSub) setting.parentSetting?.name + " " + setting.name else setting.name

                        when (setting.value) {
                            is Boolean -> (setting as Setting<Boolean?>).setValue(settings.getBoolean(settingName))
                            is Bind -> {
                                val bind = setting.value as Bind
                                val parts = settings.getString(settingName).split(":".toRegex()).toTypedArray()

                                bind.buttonCode = parts[0].toInt()
                                bind.device = java.lang.Enum.valueOf(
                                    Device::class.java, parts[1]
                                )
                            }

                            is Float -> (setting as Setting<Float?>).setValue(settings.getFloat(settingName))
                            is Double -> (setting as Setting<Double?>).setValue(settings.getDouble(settingName))
                            is Enum<*> -> {
                                val enum = setting.value as Enum<*>
                                val value = java.lang.Enum.valueOf(enum::class.java, settings.getString(settingName))

                                run breakLoop@{
                                    enum::class.java.enumConstants.forEachIndexed { index, enumValue ->
                                        if (enumValue.name == value.name) {
                                            setting.index = index
                                            return@breakLoop
                                        }
                                    }
                                }

                                (setting as Setting<Enum<*>>).setValueRaw(value)
                            }

                            is Color -> {
                                val values = settings.getString(settingName).split(":".toRegex()).toTypedArray()

                                val color = Color(
                                    values[0].toInt() / 255f,
                                    values[1].toInt() / 255f,
                                    values[2].toInt() / 255f,
                                    values[3].toFloat() / 255f
                                )

                                setting.isRainbow = java.lang.Boolean.parseBoolean(values[4])
                                setting.rainbowSpeed = values[5].toFloat()
                                setting.rainbowSaturation = values[6].toFloat()
                                setting.isSync = java.lang.Boolean.parseBoolean(values[7])
                                (setting as Setting<Color?>).setValue(color)
                            }
                        }
                    }
                }

                it.settings.forEach { setting ->
                    loadSetting(setting, false)

                    setting.subsettings.forEach { subSetting ->
                        loadSetting(subSetting, true)
                    }
                }

                if (settings.getBoolean("enabled") == !it.isEnabled) {
                    it.toggle()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Saves the social interactions
     */
    fun saveSocial() {
        if (!socialFolder.exists()) {
            socialFolder.mkdir() //Create friends folder if it doesn't already exist
        }

        val file = File("paragon${File.separator}social${File.separator}social_interactions.json")
        file.createNewFile()

        FileWriter(file).use { writer ->
            val jsonObject = JSONObject()
            val array = JSONArray()

            // Put the UUIDs into the array
            Paragon.INSTANCE.friendManager.names.forEach {
                array.put(it)
            }

            // Add array to json object
            jsonObject.putOpt("uuids", array)

            writer.write(jsonObject.toString(4))
            writer.flush()
        }
    }

    /**
     * Loads the social interactions
     */
    fun loadSocial() {
        if (!socialFolder.exists()) {
            socialFolder.mkdirs() //Create friends folder if it doesn't already exist
            return //If there is no folder there won't be anything to load either
        }

        runCatching {
            (getJSON(
                File("paragon${File.separator}social${File.separator}social_interactions.json")
            ) ?: return).getJSONArray("names").forEach {
                Paragon.INSTANCE.friendManager.addName(it.toString())
            }
        }
    }

    fun saveAlts() {
        // Create main folder if it doesn't already exist
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
        }

        val file = File("paragon${File.separator}alts.json")
        file.createNewFile()

        val jsonObject = JSONObject()

        FileWriter(file).use { writer ->
            val array = JSONArray()

            Paragon.INSTANCE.altManager.alts.forEach {
                array.put(it.email + ":" + it.password) //Put the player's info in the array - email:password
            }

            jsonObject.putOpt("alts", array)

            writer.write(jsonObject.toString(4))
            writer.flush()
        }
    }

    fun loadAlts() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
            return
        }

        runCatching {
            val json = getJSON(File("paragon${File.separator}alts.json")) ?: return
            val alts = json.getJSONArray("alts")

            //For every entry in the array, add a new alt
            for (i in 0 until alts.length()) {
                val info = alts[i].toString().split(":".toRegex()).toTypedArray()
                Paragon.INSTANCE.altManager.addAlt(Alt(info[0], info[1]))
            }
        }
    }

    fun saveOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
        }

        runCatching {
            val file = File("paragon${File.separator}client.json")
            file.createNewFile()

            FileWriter(file).use { writer ->
                val jsonObject = JSONObject()

                var prefixes = ""

                for (prefix in Paragon.INSTANCE.commandManager.commonPrefixes) {
                    prefixes += "$prefix "
                }

                jsonObject.put("ignored_prefixes", prefixes)

                writer.write(jsonObject.toString(4))
                writer.flush()
            }
        }
    }

    fun loadOther() {
        if (!mainFolder.exists()) {
            mainFolder.mkdirs()
            return
        }

        runCatching {
            val jsonObject = getJSON(File("paragon${File.separator}client.json")) ?: return

            if (jsonObject.has("ignored_prefixes")) {
                for (prefix in jsonObject.getString("ignored_prefixes").toString().split(" ".toRegex())) {
                    Paragon.INSTANCE.commandManager.commonPrefixes.add(prefix)
                }
            }
        }
    }

}
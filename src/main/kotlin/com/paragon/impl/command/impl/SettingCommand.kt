package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.ArgumentData
import com.paragon.impl.command.syntax.SyntaxBuilder
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color

/**
 * @author Surge
 * @since 16/12/2022
 */
object SettingCommand : Command("Setting", SyntaxBuilder.createBuilder(arrayListOf(
    ArgumentData("module", arrayOf("any_str")),
    ArgumentData("setting", arrayOf("any_str")),
    ArgumentData("value", arrayOf("any_str"))
))) {
    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        if (args.size < 3) {
            return false
        }

        val module = Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.name.equals(args[0], true) }.firstOrNull()

        if (module == null) {
            sendMessage("${TextFormatting.RED}Module not found!")
            return true
        }

        val settingComponents = args[1].split('.')
        
        val setting = if (settingComponents.size > 1) {
            var setting = module.settings.firstOrNull { it.name.equals(settingComponents[0], true) }

            if (setting == null) {
                sendMessage("${TextFormatting.RED}Setting not found!")
                return true
            }

            settingComponents.forEachIndexed { index, component ->
                if (index != 0) {
                    setting = setting!!.subsettings.firstOrNull { it.name.equals(component, true) }
                }
            }

            setting
        } else {
            module.settings.firstOrNull { it.name.equals(settingComponents[0], true) }
        }

        if (setting == null) {
            sendMessage("${TextFormatting.RED}Setting not found! Reminder - for subsettings, use '.' to separate the names!")
            return true
        }

        try {
            when (setting.value!!) {
                is Boolean -> (setting as Setting<Boolean?>).setValue("true" == args[2])

                is String -> {
                    var str = ""

                    args.forEachIndexed { index, arg ->
                        if (index > 1) {
                            str += arg + if (index != args.size - 1) " " else ""
                        }
                    }

                    (setting as Setting<String?>).setValue(str)
                }

                is Bind -> {
                    val bind = setting.value as Bind

                    val name = args[2]
                    val device = if (name.contains("MOUSE", true) || name.contains("BUTTON", true)) Bind.Device.MOUSE else Bind.Device.KEYBOARD
                    val code = if (device == Bind.Device.MOUSE) Mouse.getButtonIndex(name.replace("MOUSE", "BUTTON")) else Keyboard.getKeyIndex(name)

                    if (code == -1 || code == 0 && device == Bind.Device.KEYBOARD) {
                        sendMessage("${TextFormatting.RED}Invalid bind!")
                        return true
                    }

                    bind.buttonCode = code
                    bind.device = device
                }

                is Int -> (setting as Setting<Int?>).setValue(args[2].toIntOrNull() ?: setting.value)

                is Double -> (setting as Setting<Double?>).setValue(args[2].toDoubleOrNull() ?: setting.value)

                is Float -> (setting as Setting<Float?>).setValue(args[2].toFloatOrNull() ?: setting.value)

                is Enum<*> -> {
                    val enum = setting.value as Enum<*>
                    val value = java.lang.Enum.valueOf(enum::class.java, args[2].uppercase())

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
                    val values = args[2].split(":".toRegex()).toTypedArray()

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
        } catch (e: Exception) {
            e.printStackTrace()

            sendMessage("${TextFormatting.RED}Unable to change value of ${args[0]} to ${args[1]}")
            return true
        }

        sendMessage("Successfully changed value of ${setting.name} to ${ if (setting.value is Bind) (setting.value as Bind).getButtonName() else setting.value}")

        return true
    }
}
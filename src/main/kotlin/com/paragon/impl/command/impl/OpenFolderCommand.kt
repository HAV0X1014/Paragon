package com.paragon.impl.command.impl

import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.SyntaxBuilder
import net.minecraft.util.text.TextFormatting
import java.awt.Desktop
import java.io.File

object OpenFolderCommand : Command("OpenFolder", SyntaxBuilder()) {

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        Desktop.getDesktop().open(File("paragon"))
        sendMessage("${TextFormatting.GREEN}Opened Paragon folder")

        return true
    }

}
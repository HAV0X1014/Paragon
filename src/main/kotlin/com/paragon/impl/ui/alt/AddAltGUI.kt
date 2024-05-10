package com.paragon.impl.ui.alt

import com.paragon.Paragon
import com.paragon.impl.managers.alt.Alt
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.font.FontUtil.drawStringWithShadow
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.GuiTextField
import java.awt.Color
import java.io.IOException

class AddAltGUI : GuiScreen() {

    private var emailField: GuiTextField? = null
    private var passwordField: GuiTextField? = null

    override fun initGui() {
        buttonList.add(GuiButton(0, width / 2 - 80, height - 25, 75, 20, "Done"))
        buttonList.add(GuiButton(1, width / 2 + 5, height - 25, 75, 20, "Cancel"))
        emailField = GuiTextField(1, mc.fontRenderer, width / 2 - 100, height / 2 - 42, 200, 15)
        passwordField = GuiTextField(2, mc.fontRenderer, width / 2 - 100, height / 2 - 20, 200, 15)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        (emailField ?: return).drawTextBox()
        (passwordField ?: return).drawTextBox()

        if ((emailField ?: return).text.isEmpty() && !(emailField ?: return).isFocused) {
            drawStringWithShadow("Email", width / 2f - 97, height / 2f - 39, Color(150, 150, 150))
        }

        if ((passwordField ?: return).text.isEmpty() && !(passwordField ?: return).isFocused) {
            drawStringWithShadow("Password", width / 2f - 97, height / 2f - 17, Color(150, 150, 150))
        }

        FontUtil.drawCenteredString("Add Alt Account", width / 2f, 50f, Color.WHITE)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        emailField?.textboxKeyTyped(typedChar, keyCode)
        passwordField?.textboxKeyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        emailField?.mouseClicked(mouseX, mouseY, mouseButton)
        passwordField?.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> {
                Paragon.INSTANCE.altManager.addAlt(Alt((emailField ?: return).text, (passwordField ?: return).text))
                mc.displayGuiScreen(com.paragon.impl.ui.alt.AltManagerGUI())
            }

            1 -> mc.displayGuiScreen(com.paragon.impl.ui.alt.AltManagerGUI())
        }
    }

}
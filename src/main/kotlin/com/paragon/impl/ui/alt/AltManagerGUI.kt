package com.paragon.impl.ui.alt

import com.paragon.Paragon
import com.paragon.impl.managers.alt.Alt
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.util.isHovered
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiMultiplayer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.text.TextFormatting
import org.lwjgl.input.Mouse
import java.awt.Color
import java.io.IOException

class AltManagerGUI : GuiScreen() {

    private val altEntries = ArrayList<AltEntry>(3)

    override fun initGui() {
        renderString = TextFormatting.GRAY.toString() + "Idle"
        altEntries.clear()
        var offset = 150f

        for (alt in Paragon.INSTANCE.altManager.alts) {
            altEntries.add(AltEntry(alt, offset))
            offset += 20.0f
        }

        buttonList.add(GuiButton(0, 5, 5, 75, 20, "Back"))
        buttonList.add(GuiButton(1, width / 2 - 80, height - 25, 75, 20, "Add Alt"))
        buttonList.add(GuiButton(2, width / 2 + 5, height - 25, 75, 20, "Delete"))
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        buttonList[2].enabled = selectedAltEntry != null

        scroll()

        RenderUtil.drawRect(0f, 150F, width.toFloat(), 200f, Color(0, 0, 0, 200))

        RenderUtil.pushScissor(0f, 150f, width.toFloat(), 200f)

        altEntries.forEach { it.drawAlt(width) }

        RenderUtil.popScissor()

        FontUtil.drawStringWithShadow("Logged in as " + TextFormatting.GRAY + (Minecraft.getMinecraft() as IMinecraft).hookGetSession().username, 5f, 30f, Color.WHITE)
        FontUtil.drawCenteredString("Paragon Alt Manager", width / 2f, 75f, Color.WHITE)
        FontUtil.drawCenteredString(renderString, width / 2f, 100f, Color.WHITE)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun scroll() {
        val scroll = Mouse.getDWheel()

        if (altEntries.isEmpty()) {
            return
        }

        if (scroll > 0) {
            if (altEntries[0].offset < 150) {
                for (altEntry in altEntries) {
                    altEntry.offset = altEntry.offset + 10
                }
            }
            return
        }

        if (scroll < 0) {
            if (altEntries[altEntries.size - 1].offset > 340) {
                for (altEntry in altEntries) {
                    altEntry.offset = altEntry.offset - 10
                }
            }
        }
    }

    @Throws(IOException::class)
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        altEntries.forEach {
            if (isHovered(0f, 150f, width.toFloat(), 350f, mouseX, mouseY)) {
                if (isHovered(0f, it.offset, width.toFloat(), it.offset + 20, mouseX, mouseY)) {
                    if (selectedAltEntry == it) {
                        renderString = "Logging in with the email: " + it.alt.email
                        it.clicked(mouseX, mouseY, width)
                    }
                    else {
                        selectedAltEntry = it
                    }
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> com.paragon.util.mc.displayGuiScreen(GuiMultiplayer(GuiMainMenu()))
            1 -> com.paragon.util.mc.displayGuiScreen(com.paragon.impl.ui.alt.AddAltGUI())
            2 -> {
                Paragon.INSTANCE.altManager.alts.removeIf { alt: Alt -> alt.email == selectedAltEntry!!.alt.email && alt.password == selectedAltEntry!!.alt.password }
                altEntries.remove(selectedAltEntry)
            }
        }
    }

    override fun onGuiClosed() {
        Paragon.INSTANCE.storageManager.saveAlts()
    }

    companion object {
        var selectedAltEntry: AltEntry? = null
        var renderString = TextFormatting.GRAY.toString() + "Idle"
    }

}
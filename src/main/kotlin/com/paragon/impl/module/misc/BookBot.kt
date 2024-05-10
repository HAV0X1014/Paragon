package com.paragon.impl.module.misc

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IPlayerControllerMP
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.mc
import io.netty.buffer.Unpooled
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemWritableBook
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraft.util.EnumHand
import kotlin.random.Random

/**
 * @author SooStrator1136
 * @author ForgeHax
 */
object BookBot : Module("BookBot", Category.MISC, "Writes books for you") {

    private val charset = Setting("Charset", "abc")

    private val pageAmount = Setting(
        "Pages", 50F, 1F, 50F, 1F
    ) describedBy "Amount of pages that will be filled"

    private val maxChars = Setting(
        "Character Amount", 256F, 1F, 256F, 1F
    ) describedBy "The maximum of characters to write on a page"

    private val bookName = Setting("BookName", "e") describedBy "The name the book will get"

    private val delay = Setting(
        "Delay", 200.0, 0.0, 2500.0, 50.0
    ) describedBy "The delay between writing books"

    private val timer = Timer()

    override fun onTick() {
        if (mc.anyNull || !timer.hasMSPassed(delay.value)) {
            return
        }

        val book = getBook()
        if (book.first == -1) {
            return
        }

        if (mc.player.inventory.currentItem != book.first) {
            mc.player.inventory.currentItem = book.first
            (mc.playerController as IPlayerControllerMP).hookSyncCurrentPlayItem()
        }

        mc.player.openBook(book.second, EnumHand.MAIN_HAND)

        sendBook(book.second)
        mc.setIngameFocus()

        timer.reset()
    }

    /**
     * Finishs and sends the book
     */
    private fun sendBook(stack: ItemStack) {
        val pages = NBTTagList()

        repeat(pageAmount.value.toInt()) {
            pages.appendTag(NBTTagString(getRandomPageContent()))
        }

        if (stack.hasTagCompound()) {
            (stack.tagCompound ?: return).setTag("pages", pages)
        }
        else {
            stack.setTagInfo("pages", pages)
        }

        stack.setTagInfo("author", NBTTagString(mc.player.name))
        stack.setTagInfo(
            "title", NBTTagString(bookName.value)
        )

        mc.connection?.sendPacket(
            CPacketCustomPayload(
                "MC|BSign", PacketBuffer(Unpooled.buffer()).writeItemStack(stack)
            )
        )
    }

    /**
     * @return a String by the length of [maxChars] with random characters picked from the [charset]
     */
    private fun getRandomPageContent(): String {
        val builder = StringBuilder()

        repeat(maxChars.value.toInt()) {
            builder.append(charset.value[Random.nextInt(charset.value.length)])
        }

        return builder.toString()
    }

    /**
     * @return the slot of an unwritten book in the hotbar
     */
    private fun getBook(): Pair<Int, ItemStack> {
        repeat(InventoryPlayer.getHotbarSize()) {
            val stack: ItemStack? = mc.player.inventory.getStackInSlot(it)
            if (stack != null && stack != ItemStack.EMPTY && stack.item is ItemWritableBook) {
                return Pair(it, stack)
            }
        }

        return Pair(-1, ItemStack.EMPTY)
    }

}
package com.paragon.impl.command.impl

import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.SyntaxBuilder
import io.netty.buffer.Unpooled
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketBuffer

/**
 * @author EBS
 */
object SizeCommand : Command("Size", SyntaxBuilder()) {

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        val stack = Minecraft.getMinecraft().player.heldItemMainhand

        sendMessage(if (stack.isEmpty) "You are not holding any item" else "Item weighs " + getItemSize(stack).toString() + " bytes")
        return true
    }

    private fun getItemSize(stack: ItemStack): Int {
        val buff = PacketBuffer(Unpooled.buffer())
        buff.writeItemStack(stack)
        return buff.writerIndex().also { buff.release() }
    }

}
package com.paragon.impl.module.render

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.entity.RenderNametagEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.entity.EntityUtil
import com.paragon.util.mc
import com.paragon.util.player.EntityFakePlayer
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.roundToInt

/**
 * @author Surge
 */
object Nametags : Module("Nametags", Category.RENDER, "Draws nametags above players") {

    private val scaleSetting = Setting("Scale", ScaleEnum.DISTANCE) describedBy "How to scale the nametag"
    private val scaleValue = Setting("Value", 1.0, 0.0, 5.0, 0.1) describedBy "The scale value" subOf scaleSetting visibleWhen { scaleSetting.value == ScaleEnum.VALUE }

    private val health = Setting("Health", true) describedBy "Display the player's health"
    private val healthBar = Setting("HealthBar", true) describedBy "Display the player's health as a bar"
    private val ping = Setting("Ping", true) describedBy "Display the player's ping"
    private val pops = Setting("Pops", true) describedBy "Display the amount of times a player has popped totems"
    private val mainHand = Setting("Mainhand", true) describedBy "Display what the player has in their main hand"
    private val offhand = Setting("Offhand", true) describedBy "Display what the player has in their offhand"
    private val armour = Setting("Armour", true) describedBy "Display the player's current armour"
    private val enchantments = Setting("Enchantments", true) describedBy "Display the armour piece's enchantments" subOf armour
    private val durability = Setting("Durability", true) describedBy "Display the armour piece's durability" subOf armour

    override fun onRender3D() {
        // Prevent null pointer exceptions
        if (mc.anyNull || mc.player.connection == null || mc.player.ticksExisted < 20) {
            return
        }

        for (player in mc.world.playerEntities) {
            if (player === mc.player) {
               continue
            }

            val interpolated = EntityUtil.getInterpolatedPosition(player).addVector(0.0, 2.2, 0.0)

            RenderUtil.drawNametag(interpolated, scaleSetting.value == ScaleEnum.DISTANCE, if (scaleSetting.value == ScaleEnum.DISTANCE) 0.2 else scaleValue.value) {

                val builder = StringBuilder().append(player.name)

                if (health.value) {
                    builder.append(" ")
                        .append(EntityUtil.getTextColourFromEntityHealth(player))
                        .append(EntityUtil.getEntityHealth(player).roundToInt())
                }

                if (ping.value && mc.connection != null) {
                    runCatching {
                        if (player is EntityFakePlayer) {
                            builder.append(" ")
                                .append(TextFormatting.GRAY)
                                .append(-1)
                        } else {
                            builder.append(" ")
                                .append(getPingColour(mc.connection!!.getPlayerInfo(player.uniqueID).responseTime))
                                .append(mc.connection!!.getPlayerInfo(player.uniqueID).responseTime)
                        }
                    }
                }

                if (pops.value) {
                    builder.append(" ")
                        .append(TextFormatting.GOLD)
                        .append("-")
                        .append(Paragon.INSTANCE.popManager.getPops(player))
                }

                val string = builder.toString()

                val width = FontUtil.getStringWidth(string) + 4
                val height = FontUtil.getHeight() + 2 + if (healthBar.value) 2 else 0

                glTranslated(-(width / 2.0), 0.0, 0.0)

                RenderUtil.drawRect(0f, 0f, width, height, Color(0, 0, 0, 130))
                RenderUtil.drawBorder(0f, 0f, width, height, 0.5f, Color.BLACK)
                FontUtil.drawStringWithShadow(string, 1f, 1f, Color.WHITE)

                if (healthBar.value) {
                    // 3/4 will be normal health, 1/4 will be gapple
                    val healthWidth = ((width - 1) * 0.75f) * (player.health / player.maxHealth)
                    val gappleWidth = ((width - 1) * 0.25f) * (player.absorptionAmount / 16)

                    RenderUtil.drawRect(0.5f, height - 1.5f, healthWidth, 1f, Color(0, 255, 0))
                    RenderUtil.drawRect((width - 1) * 0.75f, height - 1.5f, gappleWidth, 1f, Color(255, 255, 0))
                }

                val items = arrayListOf<ItemStack>()

                if (offhand.value) {
                    items.add(player.heldItemOffhand)
                }

                if (armour.value) {
                    items.addAll(player.inventory.armorInventory.filter { it.item != Items.AIR }.reversed())
                }

                if (mainHand.value) {
                    items.add(player.heldItemMainhand)
                }

                var itemX = (width / 2f) - ((items.size * 18f) / 2f)

                items.forEach {
                    glPushMatrix()
                    glDepthMask(true)

                    // Prevent enchantment texture rendering as a square
                    GlStateManager.disableDepth()

                    RenderHelper.enableStandardItemLighting()
                    GlStateManager.scale(1f, 1f, 0f)

                    RenderUtil.drawItemStack(it, itemX, -18f, true)

                    RenderHelper.disableStandardItemLighting()
                    GlStateManager.enableDepth()

                    glPopMatrix()

                    val itemInfo = arrayListOf<String>()

                    if (durability.value && (it.item is ItemTool || it.item is ItemArmor)) {
                        val damage = 1 - it.itemDamage.toFloat() / it.maxDamage.toFloat()
                        itemInfo.add((damage * 100).toInt().toString() + "%")
                    }

                    if (enchantments.value) {
                        val enchants = it.enchantmentTagList
                        for (i in 0 until enchants.tagCount()) {
                            // Get enchant ID and level
                            val id = enchants.getCompoundTagAt(i).getInteger("id")
                            val level = enchants.getCompoundTagAt(i).getInteger("lvl")

                            // Get the enchantment
                            val enchantment = Enchantment.getEnchantmentByID(id)

                            // Make sure the enchantment is valid
                            if (enchantment != null) {
                                // Don't render if it's a curse
                                if (enchantment.isCurse) {
                                    continue
                                }

                                // Get enchantment name
                                val enchantmentName =
                                    enchantment.getTranslatedName(level).substring(0, 4) + if (level == 1) "" else level

                                // Render the enchantment's name and level
                                itemInfo.add(enchantmentName)
                            }
                        }
                    }

                    glPushMatrix()
                    glScaled(0.5, 0.5, 0.5)
                    var y = -22f

                    itemInfo.forEach { info ->
                        var colour = Color.WHITE
                        if (info.contains("%")) {
                            val green = (it.maxDamage.toFloat() - it.itemDamage.toFloat()) / it.maxDamage.toFloat()
                            val red = 1 - green
                            colour = Color(red, green, 0f, 1f)
                        }

                        FontUtil.drawStringWithShadow(info, itemX * 2, y * 2, colour)
                        y -= FontUtil.getHeight() / 2
                    }

                    glPopMatrix()

                    itemX += 18f
                }
            }

            glColor4f(1f, 1f, 1f, 1f)
        }
    }

    private fun getPingColour(ping: Int): TextFormatting {
        return when {
            ping < 0 -> TextFormatting.RED
            ping < 50 -> TextFormatting.GREEN
            ping < 150 -> TextFormatting.YELLOW
            else -> TextFormatting.RED
        }
    }

    @Listener
    fun onRenderNametag(event: RenderNametagEvent) {
        if (event.entity is EntityPlayer) {
            event.cancel()
        }
    }

    enum class ScaleEnum {
        VALUE,
        DISTANCE
    }

}
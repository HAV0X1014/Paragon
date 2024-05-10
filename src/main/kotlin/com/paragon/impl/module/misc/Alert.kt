package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.client.ModuleToggleEvent
import com.paragon.impl.event.combat.PlayerDeathEvent
import com.paragon.impl.event.combat.TotemPopEvent
import com.paragon.impl.managers.notifications.Notification
import com.paragon.impl.managers.notifications.NotificationType
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.entity.EntityUtil
import com.paragon.util.mc
import com.paragon.util.system.backgroundThread
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.EntityDonkey
import net.minecraft.entity.passive.EntityHorse
import net.minecraft.entity.passive.EntityLlama
import net.minecraft.entity.passive.EntityMule
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import java.util.*

/**
 * @author Surge, SooStrator, EBS
 * @since 21/10/2022
 */
@Aliases(["Notifications", "Notifier"])
object Alert : Module("Alert", Category.MISC, "Alerts you about certain events") {

    // misc
    private val performance = Setting("Performance", false) describedBy "Can cause notifications to appear later, only activate when needed"

    // client
    private val toggle = Setting("Toggle", false) describedBy "Notifies you when you toggle a module"

    // pops
    private val pop = Setting("Pop", true) describedBy "Notifies you when a player pops a totem"
    private val friendPop = Setting("Friends", true) describedBy "Notifies you when your friends pop" subOf pop
    private val enemyPop = Setting("Enemies", true) describedBy "Notifies you when enemies pop" subOf pop
    private val popCoords = Setting("Coords", true) describedBy "Tells you where their coords are" subOf pop

    // deaths
    private val death = Setting("Death", true) describedBy "Notifies you when a player dies"
    private val friendDeath = Setting("Friends", true) describedBy "Notifies you when your friends die" subOf death
    private val enemyDeath = Setting("Enemies", true) describedBy "Notifies you when enemies die" subOf death
    private val noPops = Setting("NoPops", true) describedBy "Notifies you even if the player hasn't popped any totems" subOf death
    private val deathCoords = Setting("Coords", true) describedBy "Tells you where their coords were" subOf death
    private val showPops = Setting("ShowPops", false) describedBy "Tells you how many times they popped" subOf death

    // armour
    private val armour = Setting("Armour", false) describedBy "Notifies when armour pieces are about to break"
    private val armourThreshold = Setting("Threshold", 20f, 1f, 75f, 1f) describedBy "The armour's durability percentage" subOf armour
    private val armourSelf = Setting("Self", true) describedBy "Notifies you when your armour pieces are about to break" subOf armour
    private val armourFriends = Setting("Friends", false) describedBy "Notifies you when your friends' armour pieces are about to break" subOf armour
    private val armourEnemies = Setting("Enemies", true) describedBy "Notifies you when your enemies' armour pieces are about to break" subOf armour

    // health
    private val health = Setting("Health", false) describedBy "Notifies when health is low"
    private val healthThreshold = Setting("Threshold", 20f, 1f, 36f, 1f) describedBy "The health threshold" subOf health
    private val healthSelf = Setting("Self", true) describedBy "Notifies you when your health is low" subOf health
    private val healthFriends = Setting("Friends", false) describedBy "Notifies you when your friends' health is low" subOf health
    private val healthEnemies = Setting("Enemies", true) describedBy "Notifies you when your enemies' health is low" subOf health

    // animals
    private val animals = Setting("Animals", false) describedBy "Notifies you when there are select animals within view distance"
    private val donkeyAlert = Setting("Donkeys", true) describedBy "Alert for Donkeys" subOf animals
    private val llamaAlert = Setting("Llamas", true) describedBy "Alert for Llamas" subOf animals
    private val horseAlert = Setting("Horses", true) describedBy "Alert for Horses" subOf animals
    private val muleAlert = Setting("Mules", true) describedBy "Alert for Mules" subOf animals
    private val animalMultiAlert = Setting("MultiAlert", false) describedBy "Alert multiple times for the same animal" subOf animals
    private val alertDelay = Setting("Delay", 200F, 0F, 5000F, 250F) describedBy "The delay between alerts" subOf animals

    private val selfWarnedArmourPieces = booleanArrayOf(false, false, false, false)
    private val othersWarnedArmourPieces: MutableMap<Entity, BooleanArray> = HashMap()

    private var warnedHealth = false
    private val othersWarnedHealth: MutableMap<EntityPlayer, Boolean> = HashMap()

    private val addedEntities: MutableList<Entity> = ArrayList(3)
    private val founds: Queue<Pair<String, Entity>> = LinkedList()
    private val timer = Timer()

    private var lastJob: Job? = null

    override fun onDisable() {
        // Reset
        Arrays.fill(selfWarnedArmourPieces, false)
        othersWarnedArmourPieces.clear()
        addedEntities.clear()
        founds.clear()
    }

    override fun onTick() {
        if (mc.anyNull) {
            // Reset
            Arrays.fill(selfWarnedArmourPieces, false)
            othersWarnedArmourPieces.clear()
            addedEntities.clear()
            founds.clear()
            return
        }

        // Launch in background thread if we want to use performance mode
        if (performance.value) {
            if (lastJob == null || (lastJob ?: return).isCompleted) {
                backgroundThread {
                    lastJob = launch {
                        check()
                    }
                }
            }
        }

        // Else just check in main thread
        else {
            check()
        }
    }

    private fun check() {
        if (armour.value) {
            if (armourSelf.value) {
                // Iterate through armour pieces
                mc.player.armorInventoryList.filter { stack -> stack != null && stack.item != Items.AIR }.forEachIndexed { index, itemStack ->
                    if (lessThanThreshold(itemStack)) {
                        // Check we haven't already warned about that armour piece
                        if (!selfWarnedArmourPieces[index]) {
                            selfWarnedArmourPieces[index] = true

                            // Notify player
                            Paragon.INSTANCE.notificationManager.addNotification(Notification(
                                "${itemStack.displayName} is about to break!",
                                NotificationType.WARNING
                            ))
                        }
                    }

                    // else, if the armour piece is above the threshold, we want to reset the warning state
                    else {
                        selfWarnedArmourPieces[index] = false
                    }
                }
            }

            if (armourFriends.value || armourEnemies.value) {
                mc.world.loadedEntityList.filter {
                    it is EntityPlayer && (Paragon.INSTANCE.friendManager.isFriend(it.name) && armourFriends.value || !Paragon.INSTANCE.friendManager.isFriend(it.name) && armourEnemies.value)
                }.forEach {
                    if (!othersWarnedArmourPieces.containsKey(it)) {
                        othersWarnedArmourPieces[it] = booleanArrayOf(false, false, false, false)
                    }

                    it.armorInventoryList.filter { stack -> stack != null && stack.item != Items.AIR }.forEachIndexed { i, stack ->
                        if (lessThanThreshold(stack)) {
                            if (!(othersWarnedArmourPieces[it] ?: return@forEachIndexed)[i]) {
                                (othersWarnedArmourPieces[it] ?: return@forEachIndexed)[i] = true
                                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                                    "${it.name}'s ${stack.displayName} is about to break!",
                                    NotificationType.WARNING
                                ))
                            }
                        }
                        else {
                            (othersWarnedArmourPieces[it] ?: return@forEachIndexed)[i] = false
                        }
                    }
                }
            }
        }

        if (health.value) {
            if (healthSelf.value) {
                if (EntityUtil.getEntityHealth(mc.player) <= healthThreshold.value) {
                    if (!warnedHealth) {
                        warnedHealth = true

                        Paragon.INSTANCE.notificationManager.addNotification(Notification(
                            "Your health is low!",
                            NotificationType.WARNING
                        ))
                    }
                } else {
                    warnedHealth = false
                }
            }

            if (healthFriends.value || healthEnemies.value) {
                mc.world.loadedEntityList.filter {
                    it != mc.player &&
                            (it is EntityPlayer && ((Paragon.INSTANCE.friendManager.isFriend(it.name) && healthFriends.value || !Paragon.INSTANCE.friendManager.isFriend(it.name) && healthEnemies.value)))
                }.forEach {
                    if (!othersWarnedHealth.containsKey(it as EntityPlayer)) {
                        othersWarnedHealth[it] = false
                    }

                    if (EntityUtil.getEntityHealth(it) <= healthThreshold.value) {
                        if (!(othersWarnedHealth[it] ?: return@forEach)) {
                            othersWarnedHealth[it] = true

                            Paragon.INSTANCE.notificationManager.addNotification(Notification(
                                "${it.name}'s health is low!",
                                NotificationType.WARNING
                            ))
                        }
                    } else {
                        othersWarnedHealth[it] = false
                    }
                }
            }
        }

        if (animals.value) {
            for (entity in mc.world.loadedEntityList) {
                if (this.founds.any { it.second == entity }) {
                    continue
                }

                val animalType: String? = when {
                    entity is EntityDonkey && donkeyAlert.value -> "Donkey"
                    entity is EntityMule && muleAlert.value -> "Mule"
                    entity is EntityLlama && llamaAlert.value -> "Llama"
                    entity is EntityHorse && horseAlert.value -> "Horse"
                    else -> null
                }

                if (animalType != null) {
                    if (!addedEntities.contains(entity) || animalMultiAlert.value) {
                        if (!animalMultiAlert.value) {
                            addedEntities.add(entity)
                        }

                        founds.add(Pair(animalType, entity))
                    }
                }
            }

            if (timer.hasMSPassed(alertDelay.value.toDouble()) && !founds.isEmpty()) {
                val nextFound = founds.poll()

                val str = "Found ${nextFound.first}! X: ${nextFound.second.posX.toInt()} Z: ${nextFound.second.posZ.toInt()}"
                Paragon.INSTANCE.notificationManager.addNotification(Notification(str, NotificationType.INFO))

                timer.reset()
            }
        }
    }

    private fun lessThanThreshold(stack: ItemStack): Boolean {
        // should fix [ArithmeticException] that was reported by ByUfo4
        if (stack.maxDamage == 0) {
            return false
        }

        return ((1 - stack.itemDamage.toFloat() / stack.maxDamage.toFloat()) * 100).toInt() <= armourThreshold.value
    }

    @Listener
    fun onModuleToggle(event: ModuleToggleEvent) {
        if (!toggle.value || event.module.isIgnored || event.module is HUDModule) {
            return
        }

        Paragon.INSTANCE.notificationManager.addNotification(Notification(
            "${event.module.name} was ${if (event.module.isEnabled) "Enabled" else "Disabled"}",
            NotificationType.INFO
        ))
    }

    @Listener
    fun onTotemPop(event: TotemPopEvent) {
        if (!pop.value) {
            return
        }

        if (friendPop.value && Paragon.INSTANCE.friendManager.isFriend(event.player.name)) {
            if (popCoords.value) {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "Your friend ${event.player.name} popped a totem at ${event.player.posX.toInt()}, ${event.player.posY.toInt()}, ${event.player.posZ.toInt()}! (${Paragon.INSTANCE.popManager.getPops(event.player)} pops)",
                    NotificationType.INFO
                ))
            } else {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "Your friend ${event.player.name} popped a totem! (${Paragon.INSTANCE.popManager.getPops(event.player)} pops)",
                    NotificationType.INFO
                ))
            }
        }

        else if (enemyPop.value && !Paragon.INSTANCE.friendManager.isFriend(event.player.name)) {
            if (popCoords.value) {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "${event.player.name} popped a totem at ${event.player.posX.toInt()}, ${event.player.posY.toInt()}, ${event.player.posZ.toInt()}! (${Paragon.INSTANCE.popManager.getPops(event.player)} pops)",
                    NotificationType.INFO
                ))
            } else {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "${event.player.name} popped a totem! (${Paragon.INSTANCE.popManager.getPops(event.player)} pops)",
                    NotificationType.INFO
                ))
            }
        }
    }

    @Listener
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!death.value || event.player == mc.player) {
            return
        }

        if (friendDeath.value && Paragon.INSTANCE.friendManager.isFriend(event.player.name)) {
            if (deathCoords.value) {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "Your friend ${event.player.name} died at ${event.player.posX.toInt()}, ${event.player.posY.toInt()}, ${event.player.posZ.toInt()}!${ if (showPops.value) " (${ Paragon.INSTANCE.popManager.getPops(event.player) } pops)" else "" }",
                    NotificationType.INFO
                ))
            } else {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "Your friend ${event.player.name} died!${ if (showPops.value) " (${ Paragon.INSTANCE.popManager.getPops(event.player) } pops)" else "" }",
                    NotificationType.INFO
                ))
            }
        }

        else if (enemyDeath.value && !Paragon.INSTANCE.friendManager.isFriend(event.player.name)) {
            if (deathCoords.value) {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "${event.player.name} died at ${event.player.posX.toInt()}, ${event.player.posY.toInt()}, ${event.player.posZ.toInt()}!${ if (showPops.value) " (${ Paragon.INSTANCE.popManager.getPops(event.player) } pops)" else "" }",
                    NotificationType.INFO
                ))
            } else {
                Paragon.INSTANCE.notificationManager.addNotification(Notification(
                    "${event.player.name} died!${ if (showPops.value) " (${ Paragon.INSTANCE.popManager.getPops(event.player) } pops)" else "" }",
                    NotificationType.INFO
                ))
            }
        }
    }

}
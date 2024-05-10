package com.paragon.util.player

import com.mojang.authlib.GameProfile
import com.paragon.util.mc
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.world.World
import java.util.*

class EntityFakePlayer(
    worldIn: World?
) : EntityOtherPlayerMP(worldIn!!, GameProfile(UUID.fromString("7880350f-d055-4608-9ad1-bb01e0af8f87"), "FakePlayer")) {

    init {
        name
        copyLocationAndAnglesFrom(mc.player)
        inventory.copyInventory(mc.player.inventory)
        mc.world.addEntityToWorld(-Int.MAX_VALUE, this)
    }

    fun despawn() {
        mc.world.removeEntity(mc.world.getEntityByID(-Int.MAX_VALUE)) //Why not just do minecraft.world.removeEntity(this) ?
    }

}
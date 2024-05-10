package com.paragon.impl.event.render.entity

import com.paragon.bus.event.CancellableEvent
import net.minecraft.client.model.ModelRenderer
import net.minecraft.entity.item.EntityEnderCrystal

/**
 * @author Surge
 */
class RenderCrystalEvent(val base: ModelRenderer?, val glass: ModelRenderer, val cube: ModelRenderer, val entity: EntityEnderCrystal, val limbSwingAmount: Float, val ageInTicks: Float, var scale: Float) : CancellableEvent()
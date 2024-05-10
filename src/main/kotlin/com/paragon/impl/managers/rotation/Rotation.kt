package com.paragon.impl.managers.rotation

/**
 * @author Surge
 * @since 23/03/22
 */
data class Rotation(
    val yaw: Float,
    val pitch: Float,
    val rotate: Rotate,
    val priority: RotationPriority,
    val threshold: Float = 55f
)
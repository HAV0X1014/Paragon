package com.paragon.impl.managers.rotation

/**
 * @author Surge
 * @since 23/03/22
 */
enum class RotationPriority(var priority: Int) {

    HIGHEST(2), HIGH(1), NORMAL(0), LOW(-1), LOWEST(-2)

}
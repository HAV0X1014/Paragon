package com.paragon.util.calculations

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.roundToInt

/**
 * @author Surge
 * @author SooStrator1136
 */
object MathsUtil {

    /**
     * Rounds a value to the nearest place
     *
     * @param value The number to round
     * @param scale  The scale
     * @return The rounded number
     */
    @JvmStatic
    fun roundDouble(value: Double, scale: Int): Double {
        return BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).toDouble()
    }

    @JvmStatic
    fun isNearlyEqual(value: Float, target: Float, threshold: Float): Boolean {
        return value >= target - threshold && value <= target + threshold
    }

    @JvmStatic
    fun getPercent(value: Double, total: Double) = value * 100 / total

    @JvmStatic
    fun getPercentOf(percent: Double, total: Double) = total * (percent / 100)

    /**
     * Rounds the given [value] to the closest possible value according to the given [incrementation].
     *
     * @param incrementation Incrementation used
     * @param value The number to round
     * @return The rounded value
     */
    @JvmStatic
    fun roundToIncrementation(incrementation: Double, value: Double): Double {
        return (value / incrementation).roundToInt() * incrementation
    }

}
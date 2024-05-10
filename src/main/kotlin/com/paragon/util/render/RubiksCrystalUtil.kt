package com.paragon.util.render

import org.lwjgl.util.vector.Quaternion

/**
 * @author rebane2001
 */
object RubiksCrystalUtil {

    @JvmField
    var cubeletStatus = arrayOf(
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion(),
        Quaternion()
    )

    @JvmField
    var cubeletLookup = arrayOf(
        arrayOf(intArrayOf(17, 9, 0), intArrayOf(20, 16, 3), intArrayOf(23, 15, 6)),
        arrayOf(intArrayOf(18, 10, 1), intArrayOf(21, -1, 4), intArrayOf(24, 14, 7)),
        arrayOf(intArrayOf(19, 11, 2), intArrayOf(22, 12, 5), intArrayOf(25, 13, 8))
    )

    @JvmField
    var cubeSides = arrayOf(
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8),
        intArrayOf(19, 18, 17, 22, 21, 20, 25, 24, 23),
        intArrayOf(0, 1, 2, 9, 10, 11, 17, 18, 19),
        intArrayOf(23, 24, 25, 15, 14, 13, 6, 7, 8),
        intArrayOf(17, 9, 0, 20, 16, 3, 23, 15, 6),
        intArrayOf(2, 11, 19, 5, 12, 22, 8, 13, 25)
    )

    @JvmField
    var cubeSideTransforms = arrayOf(
        intArrayOf(0, 0, 1),
        intArrayOf(0, 0, -1),
        intArrayOf(0, 1, 0),
        intArrayOf(0, -1, 0),
        intArrayOf(-1, 0, 0),
        intArrayOf(1, 0, 0)
    )

}
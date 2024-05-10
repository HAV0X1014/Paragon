package com.paragon.impl.ui.util

/**
 * @author Surge
 */
enum class Click(val button: Int) {

    LEFT(0), RIGHT(1), MIDDLE(2), SIDE_ONE(3), SIDE_TWO(4);

    companion object {

        @JvmStatic
        fun getClick(button: Int) = values()[button]

    }

}
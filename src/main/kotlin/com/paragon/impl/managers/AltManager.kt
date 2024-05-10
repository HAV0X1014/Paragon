package com.paragon.impl.managers

import com.paragon.impl.managers.alt.Alt

/**
 * @author SooStrator1136
 */
class AltManager {

    val alts = ArrayList<Alt>()

    fun addAlt(alt: Alt) = alts.add(alt)

}
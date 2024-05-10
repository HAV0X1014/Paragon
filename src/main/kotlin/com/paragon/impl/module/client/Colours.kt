package com.paragon.impl.module.client

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.impl.module.annotation.Constant
import com.paragon.impl.module.annotation.NotVisibleByDefault
import java.awt.Color

@Constant
@NotVisibleByDefault
object Colours : Module("Colours", Category.CLIENT, "The client's main colour") {

    @JvmField
    var mainColour = Setting(
        "MainColour", Color(185, 19, 211)
    ) describedBy "The main colour of the client"

}
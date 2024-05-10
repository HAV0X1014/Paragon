package com.paragon.impl.module.client

import com.paragon.impl.module.Module
import com.paragon.impl.module.Category
import com.paragon.impl.module.annotation.IgnoredByNotifications
import com.paragon.impl.module.annotation.NotVisibleByDefault

/**
 * @author Surge
 */
@IgnoredByNotifications
@NotVisibleByDefault
object ClientFont : Module("Font", Category.CLIENT, "Use the client's custom font")
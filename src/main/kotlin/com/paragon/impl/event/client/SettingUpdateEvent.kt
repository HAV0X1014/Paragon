package com.paragon.impl.event.client

import com.paragon.impl.setting.Setting
import com.paragon.bus.event.Event

/**
 * Fired when a settings value is changed.
 *
 * @author Surge
 */
class SettingUpdateEvent(val setting: Setting<*>) : Event()
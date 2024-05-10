package com.paragon.impl.event.render

import com.paragon.bus.event.CancellableEvent

/**
 * @author Surge
 * @since 20/11/2022
 */
class GetFramerateLimitEvent : CancellableEvent() {

    var limit = 1

}
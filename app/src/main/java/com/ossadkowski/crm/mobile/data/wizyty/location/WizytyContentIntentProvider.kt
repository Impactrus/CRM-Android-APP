package com.ossadkowski.crm.mobile.data.wizyty.location

import android.content.Intent

/**
 * Supplies the [Intent] that opens the Wizyty screen.
 *
 * The detection engine (notifier + foreground service) lives in the `data` layer and must
 * not import a concrete `ui` Activity — that would invert the layer direction. This seam is
 * implemented in the `ui` layer (which legitimately knows its own Activity) and bound via
 * Hilt, so the engine depends only on this abstraction.
 */
interface WizytyContentIntentProvider {
    fun contentIntent(): Intent
}

package com.ossadkowski.crm.mobile.ui.wizyty

import android.content.Context
import android.content.Intent
import com.ossadkowski.crm.mobile.data.wizyty.location.WizytyContentIntentProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `ui`-layer implementation of [WizytyContentIntentProvider]: builds the intent that opens
 * [WizytyActivity]. Keeping this here (rather than in the `data` engine) preserves the
 * data → ui dependency direction.
 */
@Singleton
class WizytyContentIntentProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : WizytyContentIntentProvider {
    override fun contentIntent(): Intent = Intent(context, WizytyActivity::class.java)
}

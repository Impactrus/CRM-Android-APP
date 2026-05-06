package com.ossadkowski.crm.mobile.ui.serwis.nav

import java.net.URLEncoder

/**
 * Compose-Navigation route templates for the Serwis (field-service) module.
 *
 * Argument placeholders use the standard Navigation-Compose `{arg}` syntax. Each
 * route that takes arguments exposes a small helper (`machineDetail`, `orderDetail`,
 * `workCard`) that URL-encodes the value before slotting it into the template.
 *
 * The 6 trailing routes (`PLAN`, `SCAN`, ...) are placeholders that the parallel
 * Stream 3b implementation uses for stub composables.
 */
object SerwisRoutes {
    const val TODAY = "serwis/today"
    const val MACHINES = "serwis/machines"
    const val MACHINE_DETAIL = "serwis/machine/{serial}"
    const val ORDER_DETAIL = "serwis/order/{orderNum}"
    const val WORK_CARD = "serwis/order/{orderNum}/work-card/{cardNum}"

    // Stubs (Stream 3b uses these):
    const val PLAN = "serwis/plan"
    const val SCAN = "serwis/scan"
    const val PARTS = "serwis/parts"
    const val ALERTS = "serwis/alerts"
    const val MY_TIME = "serwis/my-time"
    const val PROFILE = "serwis/profile"

    // Argument names — matching the placeholders above.
    const val ARG_SERIAL = "serial"
    const val ARG_ORDER_NUM = "orderNum"
    const val ARG_CARD_NUM = "cardNum"

    fun machineDetail(serial: String) = "serwis/machine/${serial.urlEncode()}"
    fun orderDetail(orderNum: String) = "serwis/order/${orderNum.urlEncode()}"
    fun workCard(orderNum: String, cardNum: String) =
        "serwis/order/${orderNum.urlEncode()}/work-card/${cardNum.urlEncode()}"

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, "UTF-8").replace("+", "%20")
}

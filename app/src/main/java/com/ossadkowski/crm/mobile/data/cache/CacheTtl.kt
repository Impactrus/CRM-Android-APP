package com.ossadkowski.crm.mobile.data.cache

object CacheTtl {
    const val REFERENCE = 24L * 60 * 60 * 1000   // 24h — typy, rodzaje, uzytkownicy
    const val LONG = 30L * 60 * 1000              // 30 min — calendar
    const val MODERATE = 5L * 60 * 1000           // 5 min — details, limity list
    const val PROFILE = 15L * 60 * 1000           // 15 min — auth profile
    const val LIMIT_DETAIL = 10L * 60 * 1000      // 10 min — limit detail
    const val SHORT = 2L * 60 * 1000              // 2 min — tasks, wnioski, approvals
}

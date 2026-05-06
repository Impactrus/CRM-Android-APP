package com.ossadkowski.crm.mobile.domain.serwis.model

import java.time.Instant

data class OrderLogEntry(
    val orderRegNum: String?,
    val description: String?,
    val createdBy: String?,
    val createdAt: Instant?
)

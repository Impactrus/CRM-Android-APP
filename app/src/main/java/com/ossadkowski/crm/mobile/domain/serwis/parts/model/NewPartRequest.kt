package com.ossadkowski.crm.mobile.domain.serwis.parts.model

/**
 * Input model for creating a new [PartRequest]. UI layer is responsible for
 * validating that [name] is non-blank before invoking the use case.
 */
data class NewPartRequest(
    val orderRegNum: String? = null,
    val jobCardNum: String? = null,
    val name: String,
    val partNumber: String? = null,
    val quantity: Double = 1.0,
    val unit: String = "szt",
    val notes: String? = null,
)

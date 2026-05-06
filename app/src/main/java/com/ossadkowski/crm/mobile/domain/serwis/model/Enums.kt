package com.ossadkowski.crm.mobile.domain.serwis.model

enum class OrderStatus(val code: Int) {
    OPEN(0),
    IN_PROGRESS(1),
    CLOSED(2),
    UNKNOWN(-1);

    companion object {
        fun fromCode(c: Int?): OrderStatus = values().firstOrNull { it.code == c } ?: UNKNOWN
    }
}

enum class WarrantyStatus {
    ACTIVE,
    EXPIRING_SOON,
    EXPIRED,
    UNKNOWN;

    companion object {
        fun fromString(s: String?): WarrantyStatus = when (s) {
            "active" -> ACTIVE
            "expiring_soon" -> EXPIRING_SOON
            "expired" -> EXPIRED
            else -> UNKNOWN
        }
    }
}

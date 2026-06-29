package com.ossadkowski.crm.callhistory

data class OrganizerItem(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    var lastVisitNote: String = "",
    var visitedAt: Long = 0L
)

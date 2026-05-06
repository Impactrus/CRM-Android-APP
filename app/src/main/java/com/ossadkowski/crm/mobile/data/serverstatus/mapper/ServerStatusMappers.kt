package com.ossadkowski.crm.mobile.data.serverstatus.mapper

import com.ossadkowski.crm.mobile.data.model.AuthProfileResponse
import com.ossadkowski.crm.mobile.domain.serverstatus.model.ServerStatus

fun AuthProfileResponse.toDomain(): ServerStatus {
    val parts = listOfNotNull(fName, name).map { it.trim() }.filter { it.isNotBlank() }
    val display = parts.joinToString(" ").ifBlank { username ?: "" }
    return ServerStatus(
        userId = id,
        displayName = display,
        workpost = workpost
    )
}

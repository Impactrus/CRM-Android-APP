package com.ossadkowski.crm.mobile.data.model

import com.google.gson.annotations.SerializedName

data class Handlowiec(
    @SerializedName("handlowiecId") val id: String,
    @SerializedName("imie") val imie: String?,
    @SerializedName("nazwisko") val nazwisko: String?,
    @SerializedName("stanowisko") val stanowisko: String?,
    @SerializedName("dzial") val dzial: String?,
    @SerializedName("status") val status: Int
) {
    val pelnaNazwa: String
        get() = if (!imie.isNullOrBlank() || !nazwisko.isNullOrBlank()) {
            "${imie ?: ""} ${nazwisko ?: ""}".trim()
        } else {
            id
        }
}

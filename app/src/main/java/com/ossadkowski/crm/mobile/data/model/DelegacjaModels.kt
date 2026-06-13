package com.ossadkowski.crm.mobile.data.model

import com.google.gson.annotations.SerializedName

data class DelegacjaKrajDto(
    @SerializedName("kodKraju") val kodKraju: String,
    @SerializedName("nazwaKraju") val nazwaKraju: String,
    @SerializedName("waluta") val waluta: String,
    @SerializedName("dieta") val dieta: Double,
    @SerializedName("noclegLimit") val noclegLimit: Double
)

data class KalkulatorKrajowyRequest(
    val start: String,
    val end: String,
    val sniadania: Int,
    val obiady: Int,
    val kolacje: Int,
    val kilometry: Double,
    val pojazdTyp: String,
    val pojemnoscCm3: Int,
    val liczbaNoclegow: Int
)

data class KalkulatorZagranicznyRequest(
    val start: String,
    val end: String,
    val kodKraju: String,
    val liczbaNoclegow: Int,
    val noclegiRachunkiKwota: Double
)

data class KalkulatorMieszanyRequest(
    val start: String,
    val czasPrzekroczenia: String,
    val end: String,
    val kodKraju: String,
    val kilometry: Double,
    val pojemnoscCm3: Int?,
    val liczbaNoclegowKrajowych: Int,
    val liczbaNoclegowZagranicznych: Int,
    val noclegiRachunkiKwota: Double
)

data class KalkulatorResponse(
    @SerializedName("krajowyDieta") val krajowyDieta: Double? = null,
    @SerializedName("zagranicznyDieta") val zagranicznyDieta: Double? = null,
    @SerializedName("krajowyRyczaltNocleg") val krajowyRyczaltNocleg: Double? = null,
    @SerializedName("zagranicznyRyczaltNocleg") val zagranicznyRyczaltNocleg: Double? = null,
    @SerializedName("dieta") val dieta: Double? = null,
    @SerializedName("ryczaltNocleg") val ryczaltNocleg: Double? = null,
    @SerializedName("kilometrowka") val kilometrowka: Double? = null,
    @SerializedName("kilometrowkaPln") val kilometrowkaPln: Double? = null,
    @SerializedName("dietaKrajowaPln") val dietaKrajowaPln: Double? = null,
    @SerializedName("ryczaltNoclegKrajowaPln") val ryczaltNoclegKrajowaPln: Double? = null
)

data class DelegacjaRouteDto(
    val wyjazdMiejscowosc: String?,
    val wyjazdData: String?,
    val wyjazdGodzina: String?,
    val przyjazdMiejscowosc: String?,
    val przyjazdData: String?,
    val przyjazdGodzina: String?,
    val srodekLokomocji: String,
    val kilometry: Double? = null,
    val pojemnoscCm3: Int? = null,
    val koszt: Double? = null
)

data class CreateDelegacjaRequest(
    val wniosekId: Int?,
    val wyjazdDataOd: String,
    val wyjazdDataDo: String,
    val userId: Int,
    val celMiejscowosc: String,
    val celAdres: String?,
    val celDelegacji: String,
    val srodekLokomocji: String,
    val pojazdSluzbowy: Boolean,
    val nrRejestracji: String?,
    val zaliczkaKwota: Double?,
    val poczatekPodrozy: String?,
    val startAt: String?,
    val endAt: String?,
    val oswiadczenieBhpLekarskie: Boolean,
    val typWyjazdu: String,
    val kodKraju: String?,
    val trasy: List<DelegacjaRouteDto>
)

data class CreateDelegacjaResponse(
    val id: Int,
    val message: String? = null
)

data class SubmitRozliczenieRequest(
    val trasy: List<DelegacjaRouteDto>,
    val koszty: DelegacjaKosztyDto,
    val kodKraju: String?,
    val liczbaNoclegow: Int
)

data class DelegacjaKosztyDto(
    val diety: Double,
    val noclegiRachunki: Double,
    val noclegiRyczalt: Double,
    val ryczaltyDojazdy: Double,
    val dojazdyUdokumentowane: Double,
    val inneWydatki: Double,
    val ogolem: Double,
    val uzasadnienieOdstapienia: String? = null
)

// ── Moje delegacje / Zespół / shared list item ──

data class DelegacjaListItem(
    val id: Int,
    val nrDokumentu: String? = null,
    val celDelegacji: String? = null,
    val celMiejscowosc: String? = null,
    val employeeName: String? = null,
    val status: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val typWyjazdu: String? = null,
    val zaliczkaKwota: Double? = null,
    val ogolem: Double? = null,
    val deklaracjaBhpZlozona: Boolean? = null,
    val flagaManagerUwaga: Boolean? = null
)

data class DelegacjaDetail(
    val id: Int,
    val nrDokumentu: String? = null,
    val celDelegacji: String? = null,
    val celMiejscowosc: String? = null,
    val celAdres: String? = null,
    val employeeName: String? = null,
    val status: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val typWyjazdu: String? = null,
    val srodekLokomocji: String? = null,
    val pojazdSluzbowy: Boolean? = null,
    val nrRejestracji: String? = null,
    val zaliczkaKwota: Double? = null,
    val poczatekPodrozy: String? = null,
    val oswiadczenieBhpLekarskie: Boolean? = null,
    val kodKraju: String? = null,
    val trasy: List<DelegacjaRouteDto>? = null,
    val koszty: DelegacjaKosztyDto? = null
)

// ── Finanse ──

data class DelegacjaFinanseItem(
    val id: Int,
    val nrDokumentu: String? = null,
    val employeeName: String? = null,
    val status: String? = null,
    val celMiejscowosc: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val ogolem: Double? = null,
    val zaliczkaKwota: Double? = null
)

data class DelegacjaFinanseDetail(
    val id: Int,
    val nrDokumentu: String? = null,
    val employeeName: String? = null,
    val status: String? = null,
    val celDelegacji: String? = null,
    val celMiejscowosc: String? = null,
    val startAt: String? = null,
    val endAt: String? = null,
    val typWyjazdu: String? = null,
    val zaliczkaKwota: Double? = null,
    val trasy: List<DelegacjaRouteDto>? = null,
    val koszty: DelegacjaKosztyDto? = null
)

data class DecyzjaFinanseRequest(
    val zatwierdzono: Boolean,
    val powod: String? = null
)

data class DoWyjasnieniaRequest(
    val pytanie: String
)

data class FinanseKorektaRequest(
    val diety: Double? = null,
    val noclegiRachunki: Double? = null,
    val noclegiRyczalt: Double? = null,
    val ryczaltyDojazdy: Double? = null,
    val dojazdyUdokumentowane: Double? = null,
    val inneWydatki: Double? = null,
    val ogolem: Double? = null
)

// ── HR Audit ──

data class DelegacjaAuditItem(
    val id: Int,
    val nrDokumentu: String? = null,
    val employeeName: String? = null,
    val status: String? = null,
    val deklaracjaBhpZlozona: Boolean? = null,
    val flagaManagerUwaga: Boolean? = null
)

data class DelegacjaAuditDetail(
    val delegacjaId: Int,
    val nrDokumentu: String? = null,
    val employeeName: String? = null,
    val status: String? = null,
    val statusHistory: List<StatusHistoryEntry>? = null,
    val auditLog: List<AuditLogEntry>? = null,
    val deklaracje: List<DeklaracjaEntry>? = null
)

data class StatusHistoryEntry(
    val id: Int? = null,
    val createdAt: String? = null,
    val statusFrom: String? = null,
    val statusTo: String? = null,
    val changedByUsername: String? = null,
    val reason: String? = null
)

data class AuditLogEntry(
    val id: Int? = null,
    val createdAt: String? = null,
    val action: String? = null,
    val actorUsername: String? = null,
    val ip: String? = null,
    val payload: String? = null
)

data class DeklaracjaEntry(
    val createdAt: String? = null,
    val ip: String? = null
)


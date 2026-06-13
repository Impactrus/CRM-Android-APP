package com.ossadkowski.crm.mobile.data.model

import com.google.gson.annotations.SerializedName

data class TransportVehicle(
    val id: String,
    val name: String,
    val licensePlate: String? = null,
    val status: String? = null
)

data class TransportVehiclesResponse(
    val vehicles: List<TransportVehicle>
)

data class TransportPriceListItem(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val username: String?,
    @SerializedName("ax_vend_contract_id") val axVendContractId: String?,
    @SerializedName("ax_cust_contract_id") val axCustContractId: String?,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String,
    val towar: String?,
    @SerializedName("ilosc_ton") val iloscTon: Double?,
    @SerializedName("adres_zaladunku") val adresZaladunku: String?,
    val odbiorca: String?,
    @SerializedName("adres_odbioru") val adresOdbioru: String?,
    @SerializedName("szacowany_koszt_transportu") val szacowanyKosztTransportu: Double,
    @SerializedName("zatwierdzony_koszt") val zatwierdzonyKoszt: Double?,
    val sklad: String?,
    val status: String,
    @SerializedName("reviewed_by") val reviewedBy: Int?,
    @SerializedName("reviewed_by_username") val reviewedByUsername: String?,
    @SerializedName("reviewed_at") val reviewedAt: String?,
    @SerializedName("komentarz_logistyka") val komentarzLogistyka: String?,
    @SerializedName("komentarz_handlowiec") val komentarzHandlowiec: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class TransportPriceDetailResponse(
    val request: TransportPriceListItem,
    val history: List<TransportPriceHistoryItem>
)

data class TransportPriceHistoryItem(
    val id: Int,
    val akcja: String,
    @SerializedName("stary_status") val staryStatus: String?,
    @SerializedName("nowy_status") val nowyStatus: String?,
    val komentarz: String?,
    @SerializedName("created_at") val createdAt: String?,
    val username: String?
)

data class ReviewTransportPriceRequest(
    val approved: Boolean,
    @SerializedName("zatwierdzonyKoszt") val zatwierdzonyKoszt: Double?,
    val komentarz: String?
)

data class GeocodeSuggestionDto(
    val label: String,
    val lat: Double,
    val lng: Double
)

data class CalculateRouteRequest(
    val startLat: Double,
    val startLng: Double,
    val endLat: Double,
    val endLng: Double,
    val waypoints: List<RoutePointDto> = emptyList(),
    val avoid: List<String> = emptyList(),
    val truckType: TruckTypeDto
)

data class TruckTypeDto(
    val vehicleWeight: Int,
    val vehicleHeight: Double,
    val vehicleWidth: Double,
    val vehicleLength: Double,
    val vehicleAxleWeight: Int,
    val vehicleNumberOfAxles: Int,
    val vehicleMaxSpeed: Int?
)

data class RoutePointDto(
    val latitude: Double,
    val longitude: Double
)

data class RouteResponseDto(
    val lengthInMeters: Long,
    val travelTimeInSeconds: Long,
    val points: List<RoutePointDto>
)

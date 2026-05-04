package com.ossadkowski.crm.mobile.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(
    val token: String?,
    val userId: Int?,
    val role: String?,
    val username: String?,
    val success: Boolean?,
    val message: String?,
    val dzial: String?,
    val employeeCacheId: Int?,
    val claims: Array<String>?,
    val claimsVersion: Int?
)

data class LogoutRequest(val placeholder: String? = null)

data class RefreshTokenResponse(
    val success: Boolean,
    val token: String?
)

data class AuthProfileResponse(
    val id: Int,
    @SerializedName("initials") val username: String?,
    val name: String?,
    val fName: String?,
    val role: String?,
    val departmentName: String?,
    val workpost: String?,
    val claims: Array<String>?,
    val claimsVersion: Int?
)

// ── Profile (legacy employee) ──
data class ProfileResponse(
    val id: Int?,
    val name: String?,
    val fName: String?,
    val role: String?,
    val workpost: String?,
    val departmentName: String?,
    val email: String?,
    val phone: String?,
    val restlimitd: Double?
)

// ── Paginated ──
data class PaginatedRequest(
    val page: Int,
    val pageSize: Int,
    val search: String? = null
)

data class PaginatedResponse<T>(
    @SerializedName(value = "items", alternate = ["data", "tasks", "wnioski", "results"])
    val _items: List<T>? = null,
    
    @SerializedName(value = "totalCount", alternate = ["total"])
    val _totalCount: Int? = null,
    
    @SerializedName(value = "totalPages", alternate = ["last_page"])
    val _totalPages: Int? = null
) {
    val items: List<T> get() = _items ?: emptyList()
    val totalCount: Int get() = _totalCount ?: 0
    val totalPages: Int get() = _totalPages ?: 1
}

// ── Generic page response (transport-ceny, limity-kredytowe style) ──
data class GenericPageResponse<T>(
    @SerializedName(value = "items", alternate = ["data"]) val data: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int
)

// ── Tasks (dashboard) ──
data class TaskItem(
    val id: Int,
    val title: String?,
    val description: String?,
    val status: String?,
    val assignedTo: String?,
    val createdAt: String?,
    val dueDate: String?
)

// ── Tasks V2 ──
data class TaskListItemDto(
    val id: Int,
    val templateId: Int?,
    val typ: String?,
    val tytul: String?,
    val kontrahentNazwa: String?,
    val termin: String?,
    val assignedToName: String?,
    val assignedTo: Int?,
    val status: String?,
    val isOverdue: Boolean?,
    val createdAt: String?,
    val createdByName: String?
)

data class TaskDetailDto(
    val id: Int,
    val templateId: Int?,
    val typ: String?,
    val tytul: String?,
    val opis: String?,
    val kontrahentNazwa: String?,
    val termin: String?,
    val assignedToName: String?,
    val assignedTo: Int?,
    val status: String?,
    val isOverdue: Boolean?,
    val createdAt: String?,
    val createdByName: String?,
    val createdBy: Int?,
    val totalInstances: Int?,
    val completedInstances: Int?,
    val startedAt: String?,
    val completedAt: String?
)

data class TaskCommentDto(
    val id: Int,
    val userId: Int?,
    val username: String?,
    val tresc: String?,
    val createdAt: String?
)

data class TaskFileDto(
    val id: Int,
    val nazwaPliku: String?,
    val createdAt: String?,
    val uploadedBy: String?
)

// ── Conversations ──
data class ConversationResponse(
    val items: List<ConversationItem>,
    val totalUnreadConversations: Int
)

data class ConversationItem(
    val instanceId: Int,
    val tytul: String?,
    val typ: String?,
    val status: String?,
    val kontrahentNazwa: String?,
    val totalComments: Int,
    val unreadCount: Int,
    val lastCommentAt: String?,
    val lastCommentText: String?,
    val lastCommentAuthor: String?
)
data class TaskHistoriaDto(
    val id: Int,
    val akcja: String?,
    val username: String?,
    val szczegoly: String?,
    val createdAt: String?
)

data class TaskObserverDto(
    val id: Int,
    val userId: Int?,
    val username: String?,
    val dzial: String?,
    val addedByName: String?,
    val createdAt: String?
)

data class TaskTypDto(
    val id: Int,
    val kod: String?,
    val nazwa: String?
)

data class TaskListRequest(
    val page: Int,
    val pageSize: Int,
    val search: String? = null,
    val status: String? = null,
    val typ: String? = null
)

data class ChangeTaskStatusRequest(val status: String)
data class AddTaskCommentRequest(val tresc: String)

data class CreateTaskRequest(
    val tytul: String,
    val opis: String?,
    val termin: String, // YYYY-MM-DD
    val assignedToIds: List<Int>,
    val kontrahentNazwa: String?,
    val limitKredytowyId: Int? = null,
    val typ: String = "windykacja"
)

// ── Wnioski ──
data class WnioskiListRequest(
    val userId: Int,
    val page: Int,
    val pageSize: Int
)

data class WniosekItem(
    val id: Int,
    val username: String?,
    val typ: String?,
    val odDo: String?,
    val godziny: Int?,
    val powod: String?,
    val iloscDni: Int?,
    val status: String?
)

data class WniosekDetailDto(
    val id: Int,
    val userId: Int?,
    val managerId: Int?,
    val hrId: Int?,
    val typ: String?,
    val rodzajUrlopu: String?,
    val odDo: String?,
    val godziny: Int?,
    val powod: String?,
    val iloscDni: Int?,
    val dokumenty: Int?,
    val status: String?,
    val managerApprovedAt: String?,
    val hrApprovedAt: String?,
    val createdAt: String?,
    val zastepstwoUserId: Int?,
    val zastepstwoUsername: String?,
    val zastepstwoStatus: String?,
    val komentarzManager: String?,
    val komentarzHr: String?,
    val username: String?,
    // Dane pracownika
    val oddzial: String?,
    val stanowisko: String?,
    val managerName: String?,
    val przelozonyId: Int?,
    // Limity
    val limitOpiekaDni: Int?,
    val wykorzystanoOpiekaDni: Int?,
    val limitUrlopNaZadanie: Int?,
    val wykorzystanoUrlopNaZadanie: Int?,
    val limitPracaZdalna: Int?,
    val wykorzystanoPracaZdalna: Int?
)


data class CreateWniosekRequest(
    val userId: Int,
    val typ: String,
    val rodzajUrlopu: String? = null,
    val odDo: String,
    val godziny: Int? = null,
    val powod: String,
    val iloscDni: Int,
    val dokumenty: Int? = null,
    val zastepstwoUserId: Int? = null
)

data class CreateWniosekResponse(
    val id: Int,
    val message: String?
)

data class SlownikItemDto(
    val id: Int,
    val nazwa: String
)

data class UserIdRequest(val userId: Int)

// ── Approvals ──
data class ApprovalsRequest(
    val userId: Int,
    val page: Int,
    val pageSize: Int,
    val search: String? = null,
    val role: String? = null
)

data class ManagerApprovalRequest(
    val managerId: Int,
    val approved: Boolean,
    val komentarz: String? = null,
    val data: String? = null
)

data class HrApprovalRequest(
    val hrId: Int,
    val approved: Boolean,
    val komentarz: String? = null,
    val data: String? = null
)

// ── Limity Kredytowe ──
data class LimitKredytowyListItem(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("kontrahent_account_num") val kontrahentAccountNum: String?,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String?,
    @SerializedName("obecny_limit") val obecnyLimit: Double?,
    @SerializedName("wnioskowany_limit") val wnioskowanyLimit: Double?,
    val status: String?,
    @SerializedName("ax_sync") val axSync: Boolean?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("created_by") val createdBy: String?
)

data class LimitKredytowyDetailDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("kontrahent_account_num") val kontrahentAccountNum: String?,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String?,
    @SerializedName("obecny_limit") val obecnyLimit: Double?,
    val saldo: Double?,
    val zamowione: Double?,
    @SerializedName("pozostaly_kredyt") val pozostalyKredyt: Double?,
    @SerializedName("wartosc_zabezpieczen") val wartoscZabezpieczen: Double?,
    @SerializedName("naklady_poprzedni") val nakladyPoprzedni: Double?,
    @SerializedName("naklady_biezacy") val nakladyBiezacy: Double?,
    @SerializedName("przychody_poprzedni") val przychodyPoprzedni: Double?,
    @SerializedName("przychody_biezacy") val przychodyBiezacy: Double?,
    @SerializedName("zadluzenie_przeterminowane") val zadluzeniePrzeterminowane: Double?,
    @SerializedName("wnioskowany_limit") val wnioskowanyLimit: Double?,
    @SerializedName("termin_zabezpieczen") val terminZabezpieczen: String?,
    @SerializedName("opis_zabezpieczen") val opisZabezpieczen: String?,
    @SerializedName("nowe_zabezpieczenia") val noweZabezpieczenia: String?,
    @SerializedName("dodatkowe_dochody") val dodatkoweDochody: String?,
    val zobowiazania: String?,
    val uwagi: String?,
    @SerializedName("potwierdzone_przeterminowane") val potwierdzonePrzeterminowane: Boolean?,
    @SerializedName("rozliczenie_plonami") val rozliczeniePlonami: Boolean?,
    val status: String?,
    @SerializedName("approved_by") val approvedBy: Int?,
    @SerializedName("approved_at") val approvedAt: String?,
    @SerializedName("komentarz_decyzja") val komentarzDecyzja: String?,
    @SerializedName("ax_sync") val axSync: Boolean?,
    @SerializedName("ax_data_sync") val axDataSync: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?
)

data class CreateLimitKredytowyRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("kontrahent_account_num") val kontrahentAccountNum: String,
    @SerializedName("wnioskowany_limit") val wnioskowanyLimit: Double,
    @SerializedName("termin_zabezpieczen") val terminZabezpieczen: String? = null,
    @SerializedName("opis_zabezpieczen") val opisZabezpieczen: String? = null,
    @SerializedName("nowe_zabezpieczenia") val noweZabezpieczenia: String? = null,
    @SerializedName("dodatkowe_dochody") val dodatkoweDochody: String? = null,
    val zobowiazania: String? = null,
    val uwagi: String? = null,
    @SerializedName("potwierdzone_przeterminowane") val potwierdzonePrzeterminowane: Boolean = false,
    @SerializedName("rozliczenie_plonami") val rozliczeniePlonami: Boolean = false
)

// ── Zamrożenie check ──
data class ZamrozenieCheckResponse(
    val zamrozone: Boolean,
    val dzial: String?,
    val dataOd: String?,
    val dataDo: String?,
    val opis: String?
)

// ── Calendar ──
data class ZamrozenieDto(
    val id: Int,
    val dzial: String?,
    val dataOd: String?,
    val dataDo: String?,
    val opis: String?
)

data class VacationSummaryDto(
    val previousYearDays: Double?,
    val totalDays: Double?,
    val additionalDays: Double?,
    val usedDays: Double?,
    val remainingDays: Double?
)

data class PrawoPracySaldoDto(
    val kod: String?,
    val nazwa: String?,
    val podstawaPrawna: String?,
    val jednostka: String?,
    val limitRoczny: Double?,
    val limitRocznyAlt: Double?,
    val jednostkaAlt: String?,
    val dniWyk: Double?,
    val godzinyWyk: Double?,
    val pozostalo: Double?
)

// ── HR / Home Office ──
data class HomeOfficeSaldoDto(
    val limit: Double?,
    val wykorzystane: Double?,
    val saldo: Double?,
    val rok: Int?,
    val typLimitu: String?
)

data class OvertimeSaldoDto(
    val saldo: Double?,
    val rok: Int?,
    val jednostka: String? = "godz"
)

data class HrNadgodzinyItemDto(
    val userId: Int,
    val fname: String?,
    val name: String?,
    val depart: String?,
    val rok: Int,
    val kwartal: Int,
    val godzinyNaliczone: Double?,
    val godzinyWykorzystane: Double?,
    val saldo: Double?
)

data class HrHomeOfficeLimitDto(
    val userId: Int?,
    val fname: String?,
    val name: String?,
    val rok: Int?,
    val limitDni: Double?, // json mówi "24", co bywa Double lub Int
    val typLimitu: String?,
    val wykorzystane: Double? = null 
)

data class HrPrawoPracyTypDto(
    val id: Int,
    val kod: String?,
    val wersja: Int?,
    val obowiazujeOd: String?,
    val obowiazujeDo: String?,
    val nazwa: String?,
    val podstawaPrawna: String?,
    val jednostka: String?,
    val limitRoczny: Double?,
    val limitRocznyAlt: Double?,
    val jednostkaAlt: String?,
    val wymagaZatwierdzenia: Boolean?,
    val aktywny: Boolean?,
    val opis: String?
)

data class HrOrgItemDto(
    val userId: Int,
    val displayName: String?,
    val workpost: String?,
    val dzial: String?,
    val managerId: Int?,
    val children: List<HrOrgItemDto> = emptyList()
)

// ── Kontrahenci search ──
data class KontrahentSearchItem(
    @SerializedName("id")
    val accountNum: String?,
    @SerializedName("nazwa")
    val name: String?,
    @SerializedName("adres")
    val address: String?,
    @SerializedName("nip")
    val nip: String?,
    val obecnyLimit: Double? = null
) {
    override fun toString(): String = name ?: ""
}

// ── Panel Klienta (Profile) ──
data class KontrahentProfil(
    val accountNum: String?,
    val nazwa: String?,
    val adres: String?,
    val nip: String?,
    val nrAx: String?,
    val rodzina: String?,
    val custGroup: String?,
    val partyId: String?,
    val creditMax: Double?,
    val sugerowanyLimit: Double?,
    val saldo: Double?,
    val zamowione: Double?,
    val pozostalyLimit: Double?,
    val pozostalyKredyt: Double?,
    val frozen: Boolean?,
    val frozenReason: String?,
    val blocked: Int?,
    val mandatoryCredit: Boolean?,
    // Opiekunowie
    val opiekunDka: String?,
    val opiekunDkz: String?,
    val opiekunDkm: String?,
    val opiekunBranzowy: String?,
    val opiekunKsiegowy: String?,
    val opiekunVn: String?,
    val grupaDka: String?,
    // Statystyki / Liczniki
    val syncedAt: String?,
    val windykacjaLevel: String?,
    val windykacjaOpenTasks: Int?,
    val naleznosciSuma: Double?,
    val zobowiazaniaSuma: Double?,
    val naleznosciCount: Int?,
    val zobowiazaniaCount: Int?,
    val transakcjeOtwarteCount: Int?,
    val fakturyCount: Int?,
    val wplatyCount: Int?,
    val zabezpieczeniaCount: Int? = 0,
    val wnioskiLimitCount: Int? = 0,
    val przegraneOfertyCount: Int? = 0,
    val zasiewyCount: Int? = 0,
    val historiaObrotowCount: Int? = 0,
    val topProduktyCount: Int? = 0
)

data class Naleznosc(
    val dokument: String?,
    val data: String?,
    val termin: String?,
    val kwota: Double?,
    val pozostalo: Double?,
    val waluta: String?,
    val dniPoTerminie: Int?
)

data class CreatePoleceniePracyRequest(
    val userId: Int,
    val pracownikId: Int,
    val data: String, // ISO date
    val dzienTygodnia: String, // "Sobota" lub "Niedziela"
    val godziny: Int = 8,
    val powod: String? = null
)

// --- Board / Kanban ---
data class BoardResponse(
    val columns: Map<String, BoardColumn>
)

data class BoardColumn(
    val items: List<BoardTask>,
    val hasMore: Boolean,
    val nextCursor: String?
)

data class BoardTask(
    val id: Int,
    val templateId: Int?,
    val typ: String?,
    val tytul: String?,
    val status: String?,
    val kontrahentNazwa: String?,
    val termin: String?,
    val isOverdue: Boolean,
    val assignedTo: Int?,
    val assignedToName: String?,
    val assignedToDzial: String?,
    val createdBy: Int?,
    val createdByName: String?,
    val createdAt: String?,
    val allowedTransitions: List<String> = emptyList()
)

// --- Sales ---
data class SalesOrderListItem(
    val id: Int,
    @SerializedName("nrZamowienia") val nrZam: String,
    @SerializedName("nrZamowieniaAx") val nrAx: String?,
    @SerializedName("kontrahentNazwa") val kontrahent: String?,
    val iloscTowarow: Int?,
    val wartoscNetto: Double?,
    @SerializedName("dataUtw") val dataUtworzenia: String?,
    val status: String?
)

data class SalesOrderPositionDto(
    val id: Int,
    @SerializedName("itemId") val itemId: String?,
    @SerializedName("itemName") val towar: String?,
    @SerializedName("cenaBazowa") val cenaBaz: Double?,
    val cena: Double?,
    val ilosc: Double?,
    @SerializedName("rabatKwotowy") val rabatPln: Double?,
    @SerializedName("rabatProcentowy") val rabatProcent: Double?,
    val netto: Double?,
    val magazyn: String?,
    val cennik: String?,
    @SerializedName("transSpecjalna") val trSpec: Boolean?
)

data class SalesOrderDetailDto(
    val id: Int,
    @SerializedName("nrZamowienia") val nrZam: String,
    @SerializedName("nrZamowieniaAx") val nrAx: String?,
    @SerializedName("kontrahentId") val kontrahentId: String?,
    @SerializedName("adresDostawy") val adresDostawy: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("createdBy") val createdBy: Int?,
    @SerializedName("dataNaleznosci") val dataNaleznosci: String?,
    @SerializedName("gwarancjaZaplaty") val gwarancjaZaplaty: Boolean?,
    @SerializedName("iloscTowarow") val iloscTowarow: Int?,
    @SerializedName("kontrahentAdres") val kontrahentAdres: String?,
    @SerializedName("kontrahentNazwa") val kontrahentNazwa: String?,
    @SerializedName("kontrahentNip") val kontrahentNip: String?,
    @SerializedName("metodaDostawy") val metodaDostawy: String?,
    @SerializedName("metodaZaplaty") val metodaZaplaty: String?,
    @SerializedName("platnosc") val platnosc: String?,
    @SerializedName("pozycje") val pozycje: List<SalesOrderPositionDto>?,
    val status: String?,
    @SerializedName("transDodany") val transDodany: Boolean?,
    @SerializedName("updatedAt") val updatedAt: String?,
    val uwagi: String?,
    @SerializedName("wartoscDoliczona") val wartoscDoliczona: Double?,
    @SerializedName("wartoscNetto") val wartoscNetto: Double?
)

// ── Towary ──
data class TowarListItem(
    @SerializedName("itemId") val kod: String?,
    @SerializedName("itemName") val nazwa: String?,
    @SerializedName("branzaNazwa") val branza: String?,
    @SerializedName("producentNazwa") val producent: String?,
    @SerializedName("cena") val cena: Double?,
    @SerializedName("jednostkaMiary") val jm: String?,
    @SerializedName("dostepneCalkowite") val dostepne: Double?,
    val magazyn: String?,
    val grupaNazwa: String?
)

data class KtmSlownikItem(
    val id: Int,
    val kod: String?,
    val nazwa: String?
)

data class TowaryPageResponse(
    val items: List<TowarListItem>,
    val total: Int = 0,
    val page: Int = 1,
    val pageSize: Int = 50
)

// ── Transport ──
data class TransportCenyItem(
    val id: Int,
    @SerializedName("kontrakt_ax") val kontraktAx: String?,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String?,
    @SerializedName("towar") val towar: String?,
    @SerializedName("ilosc") val ilosc: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val createdAt: String?
)

data class CreateTransportRequest(
    @SerializedName("kontrakt_ax") val kontraktAx: String?,
    @SerializedName("kontrahent_id") val kontrahentId: String,
    @SerializedName("kontrahent_nazwa") val kontrahentNazwa: String,
    @SerializedName("towar") val towar: String,
    @SerializedName("ilosc") val ilosc: Double,
    @SerializedName("sklad_id") val skladId: Int = 1,
    @SerializedName("adres_zaladunku") val adresZaladunku: String,
    @SerializedName("odbiorca") val odbiorca: String,
    @SerializedName("adres_odbioru") val adresOdbioru: String,
    @SerializedName("szacowany_koszt") val szacowanyKoszt: Double,
    @SerializedName("komentarz") val komentarz: String?
)

data class TransportAxContract(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("vendor_name") val vendorName: String?,
    @SerializedName("item_id") val itemId: String?,
    @SerializedName("item_name") val itemName: String?,
    @SerializedName("quantity") val quantity: Double?
)

// ── Umowy zbożowe ──
data class GrainContractListItem(
    val id: Int,
    @SerializedName("contractNumber") val nr: String?,
    @SerializedName("vendName") val kontrahent: String?,
    @SerializedName("itemName") val rodzajZboza: String?,
    @SerializedName("qty") val ilosc: Double?,
    val price: Double?,
    val status: String?,
    @SerializedName("transDate") val data: String?
)

data class PaymentTerm(
    val id: Int,
    val paymentTermId: String,
    val description: String,
    val numOfDays: Int
)

data class GrainContractLine(
    val id: Int? = null,
    @SerializedName("nr_kontraktu") val nrKontraktu: String?,
    @SerializedName("ilosc_ton") val iloscTon: Double,
    val cena: Double,
    @SerializedName("koszt_transportu") val kosztTransportu: Double
)

data class CreateGrainContractRequest(
    @SerializedName("data_zawarcia") val dataZawarcia: String,
    @SerializedName("rodzaj_zboza") val rodzajZboza: String,
    @SerializedName("data_zobowiazania") val dataZobowiazania: String?,
    @SerializedName("kontrahent_id") val kontrahentId: String,
    @SerializedName("fca_adres") val fcaAdres: String?,
    @SerializedName("towar_ktm") val towarKtm: String?,
    @SerializedName("ilosc_ton") val iloscTon: Double,
    @SerializedName("cena_netto") val cenaNetto: Double,
    @SerializedName("warunek_platnosci_id") val warunekPlatnosciId: Int,
    val uwagi: String?,
    val linie: List<GrainContractLine> = emptyList()
)

data class GrainContractDetail(
    val id: Int,
    @SerializedName("contractNumber") val nr: String?,
    val status: String?,
    val createdAt: String?,
    val createdByUsername: String?,
    @SerializedName("transDate") val dataZawarcia: String?,
    @SerializedName("dueDate") val dataZobowiazania: String?,
    @SerializedName("vendAccount") val kontoDostawcy: String?,
    @SerializedName("vendName") val kontrahent: String?,
    val deliveryAddress: String?,
    @SerializedName("itemName") val towar: String?,
    @SerializedName("qty") val ilosc: Double?,
    @SerializedName("price") val cena: Double?,
    val paymentTermId: String?
)

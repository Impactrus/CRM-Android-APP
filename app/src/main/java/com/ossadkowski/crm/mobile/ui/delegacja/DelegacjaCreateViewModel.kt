package com.ossadkowski.crm.mobile.ui.delegacja

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.*
import com.ossadkowski.crm.mobile.data.repository.DelegacjaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class DelegacjaCreateViewModel(
    private val repository: DelegacjaRepository = DelegacjaRepository()
) : ViewModel() {

    // Wizard navigation
    var currentStep by mutableStateOf(1)
        private set

    // Step 1 states
    var selectedType by mutableStateOf<String?>(null) // "krajowa", "zagraniczna", "mieszana"
    var selectedWniosekId by mutableStateOf<Int?>(null)
    var approvedWnioski by mutableStateOf<List<WniosekItem>>(emptyList())
    var isWnioskiLoading by mutableStateOf(false)
    var wnioskiError by mutableStateOf<String?>(null)

    // Countries dictionary
    var countriesList by mutableStateOf<List<DelegacjaKrajDto>>(emptyList())
        private set

    // Step 2 Form states
    var celDelegacji by mutableStateOf("")
    var celMiejscowosc by mutableStateOf("")
    var celAdres by mutableStateOf("")
    var zaliczkaKwota by mutableStateOf("")
    var poczatekPodrozy by mutableStateOf("")
    var startAt by mutableStateOf("") // "yyyy-MM-ddTHH:mm"
    var endAt by mutableStateOf("") // "yyyy-MM-ddTHH:mm"
    var selectedCountryCode by mutableStateOf<String?>(null)
    var oswiadczenieBhp by mutableStateOf(false)
    var nrRejestracji by mutableStateOf("")

    // Dynamic routes/legs list
    private val _routes = MutableStateFlow<List<DelegacjaRouteDto>>(listOf(createEmptyRoute()))
    val routes: StateFlow<List<DelegacjaRouteDto>> = _routes.asStateFlow()

    // Step 3 Calculator / Cost states
    var costCalculated by mutableStateOf<KalkulatorResponse?>(null)
        private set
    var isCalculating by mutableStateOf(false)
    var calculationError by mutableStateOf<String?>(null)

    // Manual cost inputs
    var manualNoclegiRachunki by mutableStateOf("")
    var manualRyczaltyDojazdy by mutableStateOf("")
    var manualDojazdyUdokumentowane by mutableStateOf("")
    var manualInneWydatki by mutableStateOf("")
    
    // Additional parameters for calculations
    var sniadaniaCount by mutableStateOf(0)
    var obiadyCount by mutableStateOf(0)
    var kolacjeCount by mutableStateOf(0)
    var liczbaNoclegow by mutableStateOf(0)
    var liczbaNoclegowKrajowych by mutableStateOf(0)
    var liczbaNoclegowZagranicznych by mutableStateOf(0)
    var czasPrzekroczenia by mutableStateOf("") // "yyyy-MM-ddTHH:mm"
    var kilometry by mutableStateOf("")
    var pojemnoscCm3 by mutableStateOf("1600") // default Capacity > 900

    // Attachments
    private val _attachments = MutableStateFlow<List<Uri>>(emptyList())
    val attachments: StateFlow<List<Uri>> = _attachments.asStateFlow()

    // Submission states
    var isSubmitting by mutableStateOf(false)
    var submitResult by mutableStateOf<NetworkResult<CreateDelegacjaResponse>?>(null)

    init {
        loadCountries()
    }

    private fun loadCountries() {
        viewModelScope.launch {
            when (val res = repository.getKraje()) {
                is NetworkResult.Success -> {
                    countriesList = res.data ?: emptyList()
                }
                else -> {}
            }
        }
    }

    fun loadApprovedWnioski(userId: Int) {
        isWnioskiLoading = true
        wnioskiError = null
        viewModelScope.launch {
            when (val res = repository.getWnioskiDelegacje(userId)) {
                is NetworkResult.Success -> {
                    approvedWnioski = res.data?.items ?: emptyList()
                    isWnioskiLoading = false
                }
                is NetworkResult.Error -> {
                    wnioskiError = res.message
                    isWnioskiLoading = false
                }
                else -> {
                    isWnioskiLoading = false
                }
            }
        }
    }

    fun selectType(type: String) {
        selectedType = type
    }

    fun applyApprovedWniosek(wniosek: WniosekItem) {
        selectedWniosekId = wniosek.id
        celDelegacji = wniosek.powod ?: ""
        celMiejscowosc = ""
        // Parse dates from wniosek.odDo e.g. "2026-05-30 – 2026-06-05"
        val dates = wniosek.odDo?.split(" – ") ?: wniosek.odDo?.split(" - ")
        if (dates != null && dates.size >= 2) {
            startAt = "${dates[0].trim()}T08:00"
            endAt = "${dates[1].trim()}T16:00"
        }
    }

    fun nextStep() {
        if (currentStep < 3) {
            currentStep++
        }
    }

    fun prevStep() {
        if (currentStep > 1) {
            currentStep--
        }
    }

    fun resetWizard() {
        currentStep = 1
        selectedType = null
        selectedWniosekId = null
        celDelegacji = ""
        celMiejscowosc = ""
        celAdres = ""
        zaliczkaKwota = ""
        poczatekPodrozy = ""
        startAt = ""
        endAt = ""
        selectedCountryCode = null
        oswiadczenieBhp = false
        nrRejestracji = ""
        _routes.value = listOf(createEmptyRoute())
        costCalculated = null
        calculationError = null
        manualNoclegiRachunki = ""
        manualRyczaltyDojazdy = ""
        manualDojazdyUdokumentowane = ""
        manualInneWydatki = ""
        sniadaniaCount = 0
        obiadyCount = 0
        kolacjeCount = 0
        liczbaNoclegow = 0
        liczbaNoclegowKrajowych = 0
        liczbaNoclegowZagranicznych = 0
        czasPrzekroczenia = ""
        kilometry = ""
        pojemnoscCm3 = "1600"
        _attachments.value = emptyList()
        submitResult = null
        isSubmitting = false
    }

    // Dynamic Route Management
    private fun createEmptyRoute() = DelegacjaRouteDto(
        wyjazdMiejscowosc = "",
        wyjazdData = "",
        wyjazdGodzina = "",
        przyjazdMiejscowosc = "",
        przyjazdData = "",
        przyjazdGodzina = "",
        srodekLokomocji = "Samochód prywatny",
        kilometry = 0.0,
        pojemnoscCm3 = 800,
        koszt = 0.0
    )

    fun addRoute() {
        _routes.value = _routes.value + createEmptyRoute()
    }

    fun removeRoute(index: Int) {
        if (_routes.value.size > 1) {
            val list = _routes.value.toMutableList()
            list.removeAt(index)
            _routes.value = list
        }
    }

    fun updateRoute(index: Int, route: DelegacjaRouteDto) {
        val list = _routes.value.toMutableList()
        if (index in list.indices) {
            list[index] = route
            _routes.value = list
        }
    }

    fun addAttachment(uri: Uri) {
        _attachments.value = _attachments.value + uri
    }

    fun removeAttachment(uri: Uri) {
        _attachments.value = _attachments.value - uri
    }

    // Calculations
    fun calculateDiety() {
        if (startAt.isBlank() || endAt.isBlank()) {
            calculationError = "Podaj początek i koniec delegacji w sekcji 'Czas trwania'."
            return
        }

        if (endAt <= startAt) {
            calculationError = "Koniec delegacji musi być po początku. Sprawdź daty."
            return
        }

        val type = selectedType ?: return
        isCalculating = true
        calculationError = null

        viewModelScope.launch {
            val privateCarRoutes = _routes.value.filter { it.srodekLokomocji == "Samochód prywatny" }
            val totalKilometry = privateCarRoutes.sumOf { it.kilometry ?: 0.0 }
            val firstPojemnosc = privateCarRoutes.firstOrNull()?.pojemnoscCm3 ?: 1600

            val result = when (type) {
                "krajowa" -> {
                    repository.kalkulatorKrajowy(
                        KalkulatorKrajowyRequest(
                            start = startAt,
                            end = endAt,
                            sniadania = sniadaniaCount,
                            obiady = obiadyCount,
                            kolacje = kolacjeCount,
                            kilometry = totalKilometry,
                            pojazdTyp = "Osobowy",
                            pojemnoscCm3 = firstPojemnosc,
                            liczbaNoclegow = liczbaNoclegow
                        )
                    )
                }
                "zagraniczna" -> {
                    val code = selectedCountryCode
                    if (code.isNullOrBlank()) {
                        isCalculating = false
                        calculationError = "Wybierz kraj delegacji przed obliczeniem."
                        return@launch
                    }
                    repository.kalkulatorZagraniczny(
                        KalkulatorZagranicznyRequest(
                            start = startAt,
                            end = endAt,
                            kodKraju = code,
                            liczbaNoclegow = liczbaNoclegow,
                            noclegiRachunkiKwota = manualNoclegiRachunki.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
                "mieszana" -> {
                    val code = selectedCountryCode
                    if (code.isNullOrBlank()) {
                        isCalculating = false
                        calculationError = "Wybierz kraj delegacji przed obliczeniem."
                        return@launch
                    }
                    if (czasPrzekroczenia.isBlank()) {
                        isCalculating = false
                        calculationError = "Podaj czas przekroczenia granicy."
                        return@launch
                    }
                    repository.kalkulatorMieszany(
                        KalkulatorMieszanyRequest(
                            start = startAt,
                            czasPrzekroczenia = czasPrzekroczenia,
                            end = endAt,
                            kodKraju = code,
                            kilometry = totalKilometry,
                            pojemnoscCm3 = firstPojemnosc,
                            liczbaNoclegowKrajowych = liczbaNoclegowKrajowych,
                            liczbaNoclegowZagranicznych = liczbaNoclegowZagranicznych,
                            noclegiRachunkiKwota = manualNoclegiRachunki.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
                else -> NetworkResult.Error("Nieznany typ wyjazdu")
            }

            when (result) {
                is NetworkResult.Success -> {
                    costCalculated = result.data
                }
                is NetworkResult.Error -> {
                    calculationError = result.message
                }
                else -> {}
            }
            isCalculating = false
        }
    }

    // Submit
    fun submitDelegacja(userId: Int, contentResolver: ContentResolver) {
        if (isSubmitting) return

        if (selectedWniosekId == null) {
            submitResult = NetworkResult.Error("Wybierz zatwierdzony wniosek delegacyjny w kroku 1.")
            return
        }

        isSubmitting = true
        submitResult = NetworkResult.Loading()

        viewModelScope.launch {
            val sType = selectedType ?: "krajowa"
            val currentRoutes = _routes.value
            val mainLocomotion = currentRoutes.firstOrNull()?.srodekLokomocji ?: "Inny"
            val hasGovCar = currentRoutes.any { it.srodekLokomocji == "Samochód służbowy" }

            val request = CreateDelegacjaRequest(
                wniosekId = selectedWniosekId,
                wyjazdDataOd = startAt,
                wyjazdDataDo = endAt,
                userId = userId,
                celMiejscowosc = celMiejscowosc,
                celAdres = celAdres.ifBlank { null },
                celDelegacji = celDelegacji,
                srodekLokomocji = mainLocomotion,
                pojazdSluzbowy = hasGovCar,
                nrRejestracji = nrRejestracji.ifBlank { null },
                zaliczkaKwota = zaliczkaKwota.toDoubleOrNull(),
                poczatekPodrozy = poczatekPodrozy.ifBlank { null },
                startAt = startAt,
                endAt = endAt,
                oswiadczenieBhpLekarskie = true,
                typWyjazdu = sType,
                kodKraju = if (sType != "krajowa") selectedCountryCode else null,
                trasy = currentRoutes
            )

            // Step 1: Create draft delegacja
            val createRes = repository.createDelegacja(request)
            if (createRes !is NetworkResult.Success || createRes.data == null) {
                val errorMsg = (createRes as? NetworkResult.Error)?.message ?: "Błąd podczas tworzenia delegacji."
                submitResult = NetworkResult.Error(errorMsg)
                isSubmitting = false
                return@launch
            }

            val delegacjaId = createRes.data.id

            // Step 2: Upload photo attachments (if any)
            _attachments.value.forEachIndexed { idx, uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val bytes = inputStream.readBytes()
                        inputStream.close()
                        val fileName = "zalacznik_delegacja_${delegacjaId}_${idx + 1}.jpg"
                        val requestBody = bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                        val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                        repository.uploadZalacznik(delegacjaId, part)
                    }
                } catch (_: Exception) {}
            }

            // Step 3: Submit rozliczenie costs
            val calc = costCalculated
            val apiDiety = (calc?.dieta ?: 0.0) + (calc?.krajowyDieta ?: 0.0) + (calc?.zagranicznyDieta ?: 0.0)
            val apiRyczaltNocleg = (calc?.ryczaltNocleg ?: 0.0) + (calc?.krajowyRyczaltNocleg ?: 0.0) + (calc?.zagranicznyRyczaltNocleg ?: 0.0)
            val kilometrowka = (calc?.kilometrowka ?: 0.0) + (calc?.kilometrowkaPln ?: 0.0)
            
            val manualNoclegi = manualNoclegiRachunki.toDoubleOrNull() ?: 0.0
            val manualDojazdy = manualRyczaltyDojazdy.toDoubleOrNull() ?: 0.0
            val manualDojUdok = manualDojazdyUdokumentowane.toDoubleOrNull() ?: 0.0
            val manualInne = manualInneWydatki.toDoubleOrNull() ?: 0.0

            val totalCost = apiDiety + apiRyczaltNocleg + kilometrowka + manualNoclegi + manualDojazdy + manualDojUdok + manualInne

            val submitRequest = SubmitRozliczenieRequest(
                trasy = currentRoutes,
                koszty = DelegacjaKosztyDto(
                    diety = apiDiety,
                    noclegiRachunki = manualNoclegi,
                    noclegiRyczalt = apiRyczaltNocleg,
                    ryczaltyDojazdy = manualDojazdy,
                    dojazdyUdokumentowane = manualDojUdok,
                    inneWydatki = manualInne,
                    ogolem = totalCost,
                    uzasadnienieOdstapienia = null
                ),
                kodKraju = if (sType != "krajowa") selectedCountryCode else null,
                liczbaNoclegow = if (sType == "mieszana") {
                    liczbaNoclegowKrajowych + liczbaNoclegowZagranicznych
                } else {
                    liczbaNoclegow
                }
            )

            val submitRes = repository.submitRozliczenie(delegacjaId, submitRequest)
            if (submitRes is NetworkResult.Success) {
                submitResult = NetworkResult.Success(createRes.data)
            } else {
                val errorMsg = (submitRes as? NetworkResult.Error)?.message ?: "Błąd podczas zatwierdzania rozliczenia."
                submitResult = NetworkResult.Error(errorMsg)
            }
            isSubmitting = false
        }
    }
}

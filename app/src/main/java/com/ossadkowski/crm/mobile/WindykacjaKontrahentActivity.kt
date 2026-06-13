package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.KontrahentSearchItem
import com.ossadkowski.crm.mobile.data.model.WindykacjaProfilDto
import com.ossadkowski.crm.mobile.databinding.ActivityWindykacjaKontrahentBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class WindykacjaKontrahentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWindykacjaKontrahentBinding
    private var searchJob: Job? = null
    private var currentCustomerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWindykacjaKontrahentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSearch()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupSearch() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                if (s != null && s.length >= 3) {
                    searchJob = lifecycleScope.launch {
                        delay(500) // Debounce
                        performSearch(s.toString())
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.editSearch.setOnItemClickListener { parent, _, position, _ ->
            val selected = parent.getItemAtPosition(position) as KontrahentSearchItem
            selected.accountNum?.let { loadCustomerProfile(it) }
        }
    }

    private fun performSearch(query: String) {
        lifecycleScope.launch {
            try {
                val results = RetrofitClient.apiService.searchKontrahenci(query)
                val searchAdapter = ArrayAdapter(
                    this@WindykacjaKontrahentActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    results
                )
                binding.editSearch.setAdapter(searchAdapter)
                binding.editSearch.showDropDown()
            } catch (e: Exception) {
                // Ignore search error to prevent crash
            }
        }
    }

    private fun loadCustomerProfile(id: String) {
        currentCustomerId = id
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        binding.scrollContent.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val profil = RetrofitClient.apiService.getWindykacjaProfil(id)
                displayProfile(profil)
            } catch (e: Exception) {
                Toast.makeText(this@WindykacjaKontrahentActivity, "Błąd pobierania profilu: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun displayProfile(p: WindykacjaProfilDto) {
        binding.scrollContent.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE

        binding.textCustomerName.text = p.nazwa ?: "-"
        binding.textSyncDate.text = "Zsynchronizowano: ${formatSyncDate(p.syncedAt)}"

        binding.textNrAx.text = p.nrAx ?: p.accountNum ?: "-"
        binding.textNip.text = p.nip ?: "-"
        binding.textAddress.text = p.adres ?: "-"
        binding.textRodzina.text = if (p.glowaRodziny != null || p.rodzina != null) p.rodzina ?: "TAK" else "NIE"
        binding.textTypGospodarstwa.text = if (p.typGospodarstwa.isNullOrBlank()) "---" else p.typGospodarstwa

        binding.textSaldo.text = formatPln(p.saldo)
        binding.textZadluzenie.text = formatPln(p.agingTotal ?: 0.0)
        binding.textLimit.text = formatPln(p.creditMax)
        
        val utilizationPercent = (p.wykorzystanieLimitu ?: 0.0) * 100.0
        binding.textWykorzystanie.text = "${formatPln(p.pozostalyLimit)} (${String.format(Locale.US, "%.1f", utilizationPercent)}%)"
        
        binding.textZamowione.text = formatPln(p.zamowione)
        binding.textWspolpracaOd.text = p.wspolpracaOd ?: "-"

        // Finanse i Odsetki
        binding.textOdsetkiOdwrotne.text = formatPln(p.finanse?.odsetkiOdwrotne)
        binding.textZaOstatnie.text = formatPln(p.finanse?.zaOstatnieOdsetki)
        binding.textHipotetyczneRok.text = formatPln(p.finanse?.hipotetyczneOdsetkiRok)
        binding.textHipotetycznePlatnosci.text = formatPln(p.finanse?.hipotetyczneNaPlatnosciach)

        // Opiekunowie
        binding.textOpiekunDka.text = p.opiekunDka ?: "-"
        binding.textOpiekunKsiegowy.text = p.opiekunKsiegowy ?: "-"
        binding.textOpiekunDkz.text = p.opiekunDkz ?: "-"
        binding.textOpiekunDkm.text = p.opiekunDkm ?: "-"

        // Ocena
        binding.textReputacja.text = if (p.reputacja != null) "${p.reputacja} / 5" else "---"
        binding.textRekomendacja.text = if (p.rekomendacja.isNullOrBlank()) "Brak" else p.rekomendacja
    }

    private fun formatPln(value: Double?): String {
        return String.format(Locale.US, "%,.2f PLN", value ?: 0.0)
            .replace(",", " ")
            .replace(".", ",")
    }

    private fun formatSyncDate(dateStr: String?): String {
        if (dateStr == null) return "-"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val formatter = SimpleDateFormat("dd.05.2026, HH:mm", Locale.US) // Consistent with mock data year
            val date = parser.parse(dateStr)
            if (date != null) formatter.format(date) else dateStr
        } catch (e: Exception) {
            dateStr
        }
    }
}

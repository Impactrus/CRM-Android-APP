package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.KontrahentProfil
import com.ossadkowski.crm.mobile.data.model.KontrahentSearchItem
import com.ossadkowski.crm.mobile.databinding.ActivityCustomerPanelBinding
import com.ossadkowski.crm.mobile.ui.sales.NaleznosciAdapter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CustomerPanelActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerPanelBinding
    private val adapterDetails = NaleznosciAdapter()
    private var searchJob: Job? = null
    private var currentCustomerId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupSearch()
        setupDashboard()
        setupRecyclerView()
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
                    this@CustomerPanelActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    results
                )
                binding.editSearch.setAdapter(searchAdapter)
                binding.editSearch.showDropDown()
            } catch (e: Exception) { }
        }
    }

    private fun loadCustomerProfile(id: String) {
        currentCustomerId = id
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        showDashboard() // Reset to dashboard when loading new customer

        lifecycleScope.launch {
            try {
                val profil = RetrofitClient.apiService.getKontrahentProfil(id)
                displayProfile(profil)
            } catch (e: Exception) {
                Toast.makeText(this@CustomerPanelActivity, "Błąd profilu: ${e.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun displayProfile(p: KontrahentProfil) {
        binding.scrollContent.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE

        binding.textCustomerName.text = p.nazwa ?: "-"
        binding.textSyncDate.text = "Dane z: ${formatSyncDate(p.syncedAt)}"
        
        binding.textAddress.text = p.adres ?: "-"
        binding.textNip.text = p.nip ?: "-"
        binding.textNrAx.text = p.nrAx ?: "-"
        binding.textGroup.text = p.custGroup ?: "-"

        binding.textSaldo.text = formatPln(p.saldo)
        binding.textZamowione.text = formatPln(p.zamowione)
        binding.textLimit.text = formatPln(p.creditMax)
        binding.textPozostalo.text = formatPln(p.pozostalyLimit)

        binding.textOpiekunDka.text = p.opiekunDka ?: "-"
        binding.textOpiekunDkz.text = p.opiekunDkz ?: "-"
        binding.textOpiekunKsiegowy.text = p.opiekunKsiegowy ?: "-"

        // Update Counts
        binding.countNaleznosci.text = (p.naleznosciCount ?: 0).toString()
        binding.countZobowiazania.text = (p.zobowiazaniaCount ?: 0).toString()
        binding.countTransakcje.text = (p.transakcjeOtwarteCount ?: 0).toString()
        binding.countZabezpieczenia.text = (p.zabezpieczeniaCount ?: 0).toString()
        binding.countFaktury.text = (p.fakturyCount ?: 0).toString()
        binding.countWplaty.text = (p.wplatyCount ?: 0).toString()
        binding.countWnioski.text = (p.wnioskiLimitCount ?: 0).toString()
        binding.countOferty.text = (p.przegraneOfertyCount ?: 0).toString()
        binding.countZasiewy.text = (p.zasiewyCount ?: 0).toString()
        binding.countHistoria.text = (p.historiaObrotowCount ?: 0).toString()
        binding.countTopProdukty.text = (p.topProduktyCount ?: 0).toString()
    }

    private fun setupDashboard() {
        binding.tileNaleznosci.setOnClickListener { showDetails("NALEŻNOŚCI") { id -> loadNaleznosci(id) } }
        binding.tileZobowiazania.setOnClickListener { showDetails("ZOBOWIĄZANIA") { /* load */ } }
        binding.tileTransakcje.setOnClickListener { showDetails("TRANSAKCJE OTWARTE") { /* load */ } }
        binding.tileZabezpieczenia.setOnClickListener { showDetails("ZABEZPIECZENIA") { /* load */ } }
        binding.tileFaktury.setOnClickListener { showDetails("FAKTURY") { /* load */ } }
        binding.tileWplaty.setOnClickListener { showDetails("WPŁATY") { /* load */ } }
        binding.tileWnioski.setOnClickListener { showDetails("WNIOSKI O LIMIT") { /* load */ } }
        binding.tileOferty.setOnClickListener { showDetails("PRZEGRANE OFERTY") { /* load */ } }
        binding.tileZasiewy.setOnClickListener { showDetails("ZASIEWY") { /* load */ } }
        binding.tileHistoria.setOnClickListener { showDetails("HISTORIA OBROTÓW") { /* load */ } }
        binding.tileTopProdukty.setOnClickListener { showDetails("TOP PRODUKTY") { /* load */ } }

        binding.btnBackToDashboard.setOnClickListener { showDashboard() }
    }

    private fun showDetails(title: String, loader: (String) -> Unit) {
        binding.gridDashboard.visibility = View.GONE
        binding.layoutDetailsHeader.visibility = View.VISIBLE
        binding.recyclerDetails.visibility = View.VISIBLE
        binding.textDetailsTitle.text = title
        
        adapterDetails.updateData(emptyList()) // Clear old data
        currentCustomerId?.let { loader(it) }
    }

    private fun showDashboard() {
        binding.gridDashboard.visibility = View.VISIBLE
        binding.layoutDetailsHeader.visibility = View.GONE
        binding.recyclerDetails.visibility = View.GONE
    }

    private fun loadNaleznosci(id: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getKontrahentNaleznosci(id, 1, 50)
                adapterDetails.updateData(response.items)
            } catch (e: Exception) {
                Toast.makeText(this@CustomerPanelActivity, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerDetails.layoutManager = LinearLayoutManager(this)
        binding.recyclerDetails.adapter = adapterDetails
    }

    private fun formatPln(value: Double?): String {
        if (value == null) return "0,00 PLN"
        val formatted = String.format("%.2f", value).replace(".", ",")
        return "$formatted PLN"
    }

    private fun formatSyncDate(dateStr: String?): String {
        if (dateStr == null) return "-"
        return try {
            // "2026-04-29T04:42:16.25723-07:00"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = inputFormat.parse(dateStr)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateStr
        }
    }
}

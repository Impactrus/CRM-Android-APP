package com.ossadkowski.crm.mobile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.SalesOrderDetailDto
import com.ossadkowski.crm.mobile.data.model.SalesOrderPositionDto
import com.ossadkowski.crm.mobile.databinding.ActivitySalesOrderDetailBinding
import com.ossadkowski.crm.mobile.ui.sales.SalesOrderPositionsAdapter
import kotlinx.coroutines.launch
import retrofit2.Response
import java.util.Calendar

class SalesOrderDetailActivity : BaseActivity() {

    private lateinit var binding: ActivitySalesOrderDetailBinding
    private var orderId: Int = -1
    private var currentDetails: SalesOrderDetailDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        orderId = intent.getIntExtra("ORDER_ID", -1)
        if (orderId == -1) {
            Toast.makeText(this, "Nieprawidłowe ID zamówienia", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupSpinners()
        
        if (orderId == 0) {
            initNewOrder()
        } else {
            loadData()
        }
    }

    private fun setupSpinners() {
        // Metoda zapłaty
        val zaplataOptions = listOf("(Brak)", "7 dni", "14 dni", "21 dni", "30 dni", "60 dni", "365 dni", "Gotówka")
        val zaplataAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, zaplataOptions)
        binding.comboMetodaZaplaty.setAdapter(zaplataAdapter)

        // Metoda dostawy
        val dostawaOptions = listOf("-- Wybierz --", "STD - Standardowy transport drogowy", "QFO - Quasi-pobranie", "SPO - Spedycja pobranie", "SOS - Specjalna", "TRA - Transport")
        val dostawaAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dostawaOptions)
        binding.comboMetodaDostawy.setAdapter(dostawaAdapter)
    }

    private fun initNewOrder() {
        binding.titleText.text = "Nowe zamówienie"
        binding.btnSendAx.isEnabled = false
        currentDetails = SalesOrderDetailDto(
            id = 0,
            nrZam = "",
            nrAx = null,
            kontrahentId = null,
            adresDostawy = null,
            createdAt = null,
            createdBy = null,
            dataNaleznosci = null,
            gwarancjaZaplaty = false,
            iloscTowarow = 0,
            kontrahentAdres = null,
            kontrahentNazwa = null,
            kontrahentNip = null,
            metodaDostawy = null,
            metodaZaplaty = null,
            platnosc = null,
            pozycje = emptyList(),
            status = "Szkic",
            transDodany = false,
            updatedAt = null,
            uwagi = null,
            wartoscDoliczona = 0.0,
            wartoscNetto = 0.0
        )
        binding.emptyPozycjeText.visibility = View.VISIBLE
        binding.recyclerPozycje.visibility = View.GONE
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }

        binding.btnSave.setOnClickListener {
            saveOrder()
        }

        binding.btnSendAx.setOnClickListener {
            prepareAx()
        }

        binding.btnAddPosition.setOnClickListener {
            val intent = Intent(this, TowarSearchActivity::class.java)
            towarSearchLauncher.launch(intent)
        }

        binding.inputKontrahent.setOnClickListener {
            val intent = Intent(this, KontrahentSearchActivity::class.java)
            kontrahentSearchLauncher.launch(intent)
        }

        binding.inputDataNaleznosci.setOnClickListener {
            showDatePicker()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val dateStr = String.format("%04d-%02d-%02d", year, month + 1, day)
            binding.inputDataNaleznosci.setText(dateStr)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private val kontrahentSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val id = data.getStringExtra("KONTRAHENT_ID")
            val nazwa = data.getStringExtra("KONTRAHENT_NAZWA")
            val adres = data.getStringExtra("KONTRAHENT_ADRES")
            val nip = data.getStringExtra("KONTRAHENT_NIP")
            
            binding.inputKontrahent.setText(nazwa)
            binding.inputAdres.setText(adres)
            binding.inputNip.setText(nip)
            
            currentDetails = currentDetails?.copy(
                kontrahentId = id,
                kontrahentNazwa = nazwa,
                kontrahentAdres = adres,
                kontrahentNip = nip
            )
        }
    }

    private val towarSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val kod = data.getStringExtra("TOWAR_KOD")
            val nazwa = data.getStringExtra("TOWAR_NAZWA")
            val cena = data.getDoubleExtra("TOWAR_CENA", 0.0)
            val ilosc = data.getDoubleExtra("ILOSC", 1.0)
            
            addPositionToOrder(kod, nazwa, cena, ilosc)
        }
    }

    private fun addPositionToOrder(kod: String?, nazwa: String?, cena: Double, ilosc: Double) {
        val details = currentDetails ?: return
        val currentPozycje = details.pozycje?.toMutableList() ?: mutableListOf()
        
        val newPos = SalesOrderPositionDto(
            id = 0,
            itemId = kod,
            towar = nazwa,
            cenaBaz = cena,
            cena = cena,
            ilosc = ilosc,
            rabatPln = 0.0,
            rabatProcent = 0.0,
            netto = cena * ilosc,
            magazyn = null,
            cennik = null,
            trSpec = false
        )
        
        currentPozycje.add(newPos)
        
        val updated = details.copy(
            pozycje = currentPozycje,
            wartoscNetto = (details.wartoscNetto ?: 0.0) + (newPos.netto ?: 0.0),
            iloscTowarow = (details.iloscTowarow ?: 0) + 1
        )
        
        currentDetails = updated
        displayOrderDetails(updated)
    }

    private fun saveOrder() {
        val details = currentDetails ?: return
        
        val updatedDetails = details.copy(
            kontrahentNazwa = binding.inputKontrahent.text.toString(),
            kontrahentAdres = binding.inputAdres.text.toString(),
            kontrahentNip = binding.inputNip.text.toString(),
            platnosc = binding.inputPlatnosc.text.toString(),
            metodaZaplaty = binding.comboMetodaZaplaty.text.toString(),
            metodaDostawy = binding.comboMetodaDostawy.text.toString(),
            dataNaleznosci = binding.inputDataNaleznosci.text.toString(),
            adresDostawy = binding.inputAdresDostawy.text.toString(),
            wartoscDoliczona = binding.inputWartoscDoliczona.text.toString().toDoubleOrNull() ?: 0.0,
            uwagi = binding.inputUwagi.text.toString(),
            transDodany = binding.checkTransDodany.isChecked,
            gwarancjaZaplaty = binding.checkGwarancjaZaplaty.isChecked
        )
        
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                if (orderId == 0) {
                    val response = RetrofitClient.apiService.createSalesOrder(updatedDetails)
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val newOrder = response.body()
                        if (newOrder != null) {
                            orderId = newOrder.id
                            currentDetails = newOrder
                            binding.titleText.text = "Zamówienie #${newOrder.nrZam}"
                            binding.btnSendAx.isEnabled = true
                            Toast.makeText(this@SalesOrderDetailActivity, "Zamówienie utworzone", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SalesOrderDetailActivity, "Błąd zapisu: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val response = RetrofitClient.apiService.updateSalesOrder(orderId, updatedDetails)
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@SalesOrderDetailActivity, "Zmiany zapisane", Toast.LENGTH_SHORT).show()
                        currentDetails = updatedDetails
                    } else {
                        Toast.makeText(this@SalesOrderDetailActivity, "Błąd zapisu: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SalesOrderDetailActivity, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareAx() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.prepareSalesOrderAx(orderId)
                binding.progressBar.visibility = View.GONE
                showAxConfirmDialog(response)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SalesOrderDetailActivity, "Błąd przygotowania AX: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAxConfirmDialog(details: SalesOrderDetailDto) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Wyślij do AX")
            .setMessage("Czy na pewno chcesz wysłać zamówienie do systemu AX?\n\nKontrahent: ${details.kontrahentNazwa}\nWartość netto: ${details.wartoscNetto} PLN")
            .setPositiveButton("Potwierdź i wyślij") { _, _ ->
                sendToAx()
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun sendToAx() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.sendSalesOrderToAx(orderId)
                binding.progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(this@SalesOrderDetailActivity, "Zamówienie wysłane do AX", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@SalesOrderDetailActivity, "Błąd wysyłki: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SalesOrderDetailActivity, "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getSalesOrderDetails(orderId)
                currentDetails = response
                binding.progressBar.visibility = View.GONE
                displayOrderDetails(response)
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@SalesOrderDetailActivity, "Błąd wczytywania: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayOrderDetails(response: SalesOrderDetailDto) {
        binding.titleText.text = "Zamówienie #${response.nrZam}"
        
        binding.inputKontrahent.setText(response.kontrahentNazwa ?: "")
        binding.inputAdres.setText(response.kontrahentAdres ?: "")
        binding.inputNip.setText(response.kontrahentNip ?: "")
        binding.inputPlatnosc.setText(response.platnosc ?: "")
        binding.comboMetodaZaplaty.setText(response.metodaZaplaty ?: "", false)
        binding.comboMetodaDostawy.setText(response.metodaDostawy ?: "", false)
        binding.inputDataNaleznosci.setText(response.dataNaleznosci ?: "")
        binding.inputAdresDostawy.setText(response.adresDostawy ?: "")
        binding.inputWartoscDoliczona.setText((response.wartoscDoliczona ?: 0.0).toString())
        binding.inputUwagi.setText(response.uwagi ?: "")
        binding.checkTransDodany.isChecked = response.transDodany ?: false
        binding.checkGwarancjaZaplaty.isChecked = response.gwarancjaZaplaty ?: false
        
        if (response.pozycje.isNullOrEmpty()) {
            binding.emptyPozycjeText.visibility = View.VISIBLE
            binding.recyclerPozycje.visibility = View.GONE
        } else {
            binding.emptyPozycjeText.visibility = View.GONE
            binding.recyclerPozycje.visibility = View.VISIBLE
            binding.recyclerPozycje.layoutManager = LinearLayoutManager(this)
            val positionsAdapter = SalesOrderPositionsAdapter()
            binding.recyclerPozycje.adapter = positionsAdapter
            positionsAdapter.submitList(response.pozycje ?: emptyList())
        }
    }
}

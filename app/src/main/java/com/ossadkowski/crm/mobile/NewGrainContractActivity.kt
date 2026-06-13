package com.ossadkowski.crm.mobile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.CreateGrainContractRequest
import com.ossadkowski.crm.mobile.data.model.PaymentTerm
import com.ossadkowski.crm.mobile.databinding.ActivityNewGrainContractBinding
import com.ossadkowski.crm.mobile.ui.sales.GrainContractsViewModel
import java.text.SimpleDateFormat
import java.util.*

class NewGrainContractActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityNewGrainContractBinding
    private val viewModel: GrainContractsViewModel by viewModels()
    
    private var selectedKontrahentId: String? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var paymentTerms: List<PaymentTerm> = emptyList()

    private val kontrahentSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            selectedKontrahentId = data?.getStringExtra("KONTRAHENT_ID")
            val name = data?.getStringExtra("KONTRAHENT_NAZWA")
            binding.inputKontrahent.setText(name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewGrainContractBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSession()
        setupDrawer(
            binding.drawerLayout,
            binding.menuButton,
            binding.includeDrawer.drawerClose,
            binding.includeDrawer.drawerName,
            binding.includeDrawer.drawerRole,
            binding.includeDrawer.drawerPanel,
            binding.includeDrawer.drawerApprovals,
            binding.includeDrawer.drawerTasks,
            binding.includeDrawer.drawerCalendar,
            binding.includeDrawer.drawerWindykacjaHeader,
            binding.includeDrawer.drawerWindykacjaArrow,
            binding.includeDrawer.drawerWindykacjaSub,
            binding.includeDrawer.drawerWindykacjaWnioski,
            binding.includeDrawer.drawerWindykacjaNowy,
            binding.includeDrawer.drawerWindykacjaZadania,
            binding.includeDrawer.drawerHrHeader,
            binding.includeDrawer.drawerHrArrow,
            binding.includeDrawer.drawerHrSub,
            binding.includeDrawer.drawerHrAkceptacje,
            binding.includeDrawer.drawerHrHistoria,
            binding.includeDrawer.drawerHrKalendarz,
            binding.includeDrawer.drawerHrNadgodziny,
            binding.includeDrawer.drawerHrHomeOffice,
            binding.includeDrawer.drawerHrPrawoPracy,
            binding.includeDrawer.drawerHrSchemat,
            binding.includeDrawer.drawerSalesHeader,
            binding.includeDrawer.drawerSalesArrow,
            binding.includeDrawer.drawerSalesSub,
            binding.includeDrawer.drawerSalesOrders,
            binding.includeDrawer.drawerSalesTransport,
            binding.includeDrawer.drawerSalesContracts,
            binding.includeDrawer.drawerSalesReps,
            binding.includeDrawer.drawerSalesClientPanel,
            binding.includeDrawer.drawerSalesClients,
            binding.includeDrawer.drawerSalesGrainTrade,
            binding.includeDrawer.drawerLogout,
            binding.includeDrawer.drawerKontrahenci,
            binding.includeDrawer.drawerTowary,
            binding.includeDrawer.drawerZamowienia,
            binding.includeDrawer.drawerTransakcje,
            binding.includeDrawer.drawerWizyty,
            binding.includeDrawer.drawerOferty,
            binding.includeDrawer.drawerCrm,
            binding.includeDrawer.drawerInfo,
            binding.includeDrawer.drawerMessages,
            binding.includeDrawer.drawerWindykacjaProfil
        )

        setupUI()
        observeViewModel()
        
        viewModel.loadPaymentTerms()
    }

    private fun setupUI() {
        // Daty
        val today = Calendar.getInstance()
        binding.inputDataZawarcia.setText(dateFormat.format(today.time))
        
        binding.inputDataZawarcia.setOnClickListener { showDatePicker(it as android.widget.EditText) }
        binding.inputDataZobowiazania.setOnClickListener { showDatePicker(it as android.widget.EditText) }

        // Spinner Rodzaj Zboża
        val grainTypes = listOf("-- Wybierz --", "Pszenica konsumpcyjna", "Pszenica paszowa", "Rzepak", "Kukurydza", "Jęczmień", "Żyto", "Pszenżyto", "Inne")
        val grainAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grainTypes)
        grainAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRodzajZboza.adapter = grainAdapter

        // Kontrahent
        binding.inputKontrahent.setOnClickListener {
            kontrahentSearchLauncher.launch(Intent(this, KontrahentSearchActivity::class.java))
        }

        binding.btnCancel.setOnClickListener { finish() }
        binding.btnSubmit.setOnClickListener { submitForm() }
    }

    private fun showDatePicker(editText: android.widget.EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance()
            selected.set(year, month, dayOfMonth)
            editText.setText(dateFormat.format(selected.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun observeViewModel() {
        viewModel.paymentTerms.observe(this) { result ->
            if (result is NetworkResult.Success) {
                paymentTerms = result.data ?: emptyList()
                val descriptions = paymentTerms.map { it.description }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, descriptions)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerWarunekPlatnosci.adapter = adapter
            }
        }

        viewModel.createResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text = "Wysyłanie..."
                }
                is NetworkResult.Success -> {
                    Toast.makeText(this, "Umowa wysłana do zatwierdzenia", Toast.LENGTH_LONG).show()
                    finish()
                }
                is NetworkResult.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Wyślij do zatwierdzenia"
                    Toast.makeText(this, "Błąd: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun submitForm() {
        val rodzajZboza = binding.spinnerRodzajZboza.selectedItem.toString()
        if (rodzajZboza == "-- Wybierz --") {
            Toast.makeText(this, "Wybierz rodzaj zboża", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedKontrahentId == null) {
            Toast.makeText(this, "Wybierz kontrahenta", Toast.LENGTH_SHORT).show()
            return
        }

        val ilosc = binding.inputIlosc.text.toString().toDoubleOrNull() ?: 0.0
        val cena = binding.inputCena.text.toString().toDoubleOrNull() ?: 0.0
        
        if (ilosc <= 0 || cena <= 0) {
            Toast.makeText(this, "Wprowadź poprawną ilość i cenę", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedTerm = paymentTerms.getOrNull(binding.spinnerWarunekPlatnosci.selectedItemPosition)
        if (selectedTerm == null) {
            Toast.makeText(this, "Wybierz warunek płatności", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateGrainContractRequest(
            dataZawarcia = binding.inputDataZawarcia.text.toString(),
            rodzajZboza = rodzajZboza,
            dataZobowiazania = binding.inputDataZobowiazania.text.toString().takeIf { it.isNotEmpty() },
            kontrahentId = selectedKontrahentId!!,
            fcaAdres = binding.inputFca.text.toString(),
            towarKtm = binding.inputTowarKtm.text.toString(),
            iloscTon = ilosc,
            cenaNetto = cena,
            warunekPlatnosciId = selectedTerm.id,
            uwagi = binding.inputUwagi.text.toString()
        )

        viewModel.createContract(request)
    }

    override fun performLogout() {
        sessionManager.clear()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}

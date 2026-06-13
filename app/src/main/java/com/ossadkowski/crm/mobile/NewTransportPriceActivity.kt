package com.ossadkowski.crm.mobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.model.CreateTransportRequest
import com.ossadkowski.crm.mobile.databinding.ActivityNewTransportPriceBinding
import com.ossadkowski.crm.mobile.ui.sales.AxContractSearchActivity
import com.ossadkowski.crm.mobile.ui.sales.TransportViewModel

class NewTransportPriceActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityNewTransportPriceBinding
    private val viewModel: TransportViewModel by viewModels()

    private var selectedAxContractId: String? = null
    private var selectedKontrahentId: String? = null

    private val axSearchLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            selectedAxContractId = data?.getStringExtra("AX_CONTRACT_ID")
            binding.inputAx.setText(selectedAxContractId)
            
            // Auto-fill other fields if available
            val vendorName = data?.getStringExtra("AX_VENDOR_NAME")
            if (!vendorName.isNullOrEmpty()) {
                binding.inputKontrahent.setText(vendorName)
                selectedKontrahentId = "AX_IMPORT" // Placeholder or handled by backend
            }
            
            val itemName = data?.getStringExtra("AX_ITEM_NAME")
            if (!itemName.isNullOrEmpty()) {
                binding.inputTowar.setText(itemName)
            }
            
            val quantity = data?.getDoubleExtra("AX_QUANTITY", 0.0) ?: 0.0
            if (quantity > 0) {
                binding.inputIlosc.setText(quantity.toString())
            }
        }
    }

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
        binding = ActivityNewTransportPriceBinding.inflate(layoutInflater)
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
    }

    private fun setupUI() {
        binding.inputAx.setOnClickListener {
            axSearchLauncher.launch(Intent(this, AxContractSearchActivity::class.java))
        }

        binding.inputKontrahent.setOnClickListener {
            kontrahentSearchLauncher.launch(Intent(this, KontrahentSearchActivity::class.java))
        }

        binding.btnCancel.setOnClickListener { finish() }

        binding.btnSubmit.setOnClickListener {
            submitForm()
        }
    }

    private fun submitForm() {
        val kontrahentNazwa = binding.inputKontrahent.text.toString()
        val towar = binding.inputTowar.text.toString()
        val iloscStr = binding.inputIlosc.text.toString()
        val kosztStr = binding.inputKoszt.text.toString()
        
        if (kontrahentNazwa.isEmpty() || towar.isEmpty() || iloscStr.isEmpty() || kosztStr.isEmpty()) {
            Toast.makeText(this, "Proszę wypełnić wymagane pola (*)", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateTransportRequest(
            kontraktAx = selectedAxContractId,
            kontrahentId = selectedKontrahentId ?: "",
            kontrahentNazwa = kontrahentNazwa,
            towar = towar,
            ilosc = iloscStr.toDoubleOrNull() ?: 0.0,
            skladId = 1,
            adresZaladunku = binding.inputAdresZaladunku.text.toString(),
            odbiorca = binding.inputOdbiorca.text.toString(),
            adresOdbioru = binding.inputAdresOdbioru.text.toString(),
            szacowanyKoszt = kosztStr.toDoubleOrNull() ?: 0.0,
            komentarz = binding.inputKomentarz.text.toString()
        )

        viewModel.createTransportPrice(request)
    }

    private fun observeViewModel() {
        viewModel.createResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text = "Wysyłanie..."
                }
                is NetworkResult.Success -> {
                    Toast.makeText(this, "Wniosek wysłany do logistyki", Toast.LENGTH_LONG).show()
                    finish()
                }
                is NetworkResult.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = "Wyślij do logistyki"
                    Toast.makeText(this, "Błąd: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun performLogout() {
        // Handled by BaseActivity or Dashboard if needed, but here we just clear session
        sessionManager.clear()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}

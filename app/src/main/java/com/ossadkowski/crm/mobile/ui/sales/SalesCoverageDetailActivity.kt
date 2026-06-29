package com.ossadkowski.crm.mobile.ui.sales

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.ossadkowski.crm.mobile.BaseDrawerActivity
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivitySalesCoverageDetailBinding
import java.util.Locale

class SalesCoverageDetailActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivitySalesCoverageDetailBinding
    private val viewModel: SalesCoverageDetailViewModel by viewModels()
    private var contractId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesCoverageDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contractId = intent.getStringExtra("CONTRACT_ID") ?: ""
        if (contractId.isEmpty()) {
            Toast.makeText(this, "Brak identyfikatora kontraktu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initSession()
        setupDrawer(
            binding.drawerLayout,
            View(this), // menuButton (not used in detail toolbar, we use back button instead)
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

        viewModel.loadDetail(contractId)
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadDetail(contractId)
        }
    }

    private fun observeViewModel() {
        viewModel.detail.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    val data = result.data
                    if (data != null) {
                        bindData(data)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this, "Błąd: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun bindData(data: com.ossadkowski.crm.mobile.data.model.SalesCoverageDetailResponse) {
        val header = data.header ?: return

        // Header info
        binding.textToolbarTitle.text = "Kontrakt ${header.contractNumber}"
        binding.textDetailContractHeader.text = "Kontrakt klienta z AX: ${header.contractNumber ?: "-"}"
        binding.textDetailAxStatus.text = "Status AX: ${header.status ?: "Nieznany"}"
        
        val covPercent = (header.coveragePercent ?: 0.0).toInt()
        val delPercent = (header.deliveryPercent ?: 0.0).toInt()
        binding.textDetailCoveragePercent.text = "Pokrycie: $covPercent%"
        binding.textDetailDeliveryPercent.text = "Dostarczono: $delPercent%"

        // Metrics Grid
        binding.metricSalesQty.text = String.format(Locale.getDefault(), "%,.2f t", header.salesQty ?: 0.0)
        binding.metricCoverageQty.text = String.format(Locale.getDefault(), "%,.2f t", header.coverageQty ?: 0.0)
        binding.metricGapQty.text = String.format(Locale.getDefault(), "%,.2f t", header.gapQty ?: 0.0)
        binding.metricSalesPrice.text = String.format(Locale.getDefault(), "%,.2f PLN/t", header.price ?: 0.0)
        
        // Staging raw json returns avgPurchasePrice inside the parent object. Since models define fields from header, 
        // let's grab from header fields.
        val avgPurchase = header.price?.minus(header.spread ?: 0.0) ?: 0.0
        binding.metricPurchasePrice.text = String.format(Locale.getDefault(), "%,.2f PLN/t", avgPurchase)
        binding.metricSpread.text = String.format(Locale.getDefault(), "%+,.2f PLN/t", header.spread ?: 0.0)

        // Customer details
        binding.textClientInfo.text = "${header.clientName ?: "-"} (Konto: ${data.customerAccount ?: "-"})"
        binding.textDetailItem.text = header.itemName ?: "-"
        binding.textDetailTermin.text = "${header.dkz ?: "-"} (Termin: ${header.dueDate?.take(10) ?: "-"})"

        // Margins Calculations
        val sQty = header.salesQty ?: 0.0
        val sPrice = header.price ?: 0.0
        val salesValue = sQty * sPrice
        
        val cQty = header.coverageQty ?: 0.0
        val pPrice = avgPurchase
        val purchaseValue = cQty * pPrice
        
        val marginValue = salesValue - purchaseValue

        binding.labelMarginSales.text = String.format(Locale.getDefault(), "Sprzedaż: %,.1f t * %,.2f PLN/t", sQty, sPrice)
        binding.valMarginSales.text = String.format(Locale.getDefault(), "%,.2f PLN", salesValue)

        binding.labelMarginPurchase.text = String.format(Locale.getDefault(), "Zakup: %,.1f t * %,.2f PLN/t", cQty, pPrice)
        binding.valMarginPurchase.text = String.format(Locale.getDefault(), "%,.2f PLN", purchaseValue)

        binding.valSpreadTotal.text = String.format(Locale.getDefault(), "%,.2f PLN", marginValue)

        // Purchases List
        val purchases = data.purchases ?: emptyList()
        if (purchases.isEmpty()) {
            binding.textDetailPurchasesList.text = "Brak podpiętych zakupów"
        } else {
            val sb = StringBuilder()
            purchases.forEach { purchase ->
                sb.append("• Kontrakt: ${purchase.contractNumber ?: "-"}\n")
                sb.append("  Dostawca: ${purchase.vendName ?: "-"}\n")
                sb.append("  Ilość: ${String.format(Locale.getDefault(), "%,.2f t", purchase.qty ?: 0.0)} | Cena: ${String.format(Locale.getDefault(), "%,.2f PLN/t", purchase.price ?: 0.0)}\n")
                sb.append("  Status: ${purchase.status ?: "Brak"}\n\n")
            }
            binding.textDetailPurchasesList.text = sb.toString().trimEnd()
        }
    }

    override fun performLogout() {
        sessionManager.clear()
        startActivity(android.content.Intent(this, com.ossadkowski.crm.mobile.MainActivity::class.java))
        finishAffinity()
    }
}

package com.ossadkowski.crm.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityTransportListBinding
import com.ossadkowski.crm.mobile.ui.sales.TransportListAdapter
import com.ossadkowski.crm.mobile.ui.sales.TransportViewModel

class TransportListActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityTransportListBinding
    private val viewModel: TransportViewModel by viewModels()
    private val adapter = TransportListAdapter { item ->
        // Potencjalnie przejście do szczegółów
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransportListBinding.inflate(layoutInflater)
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
        
        viewModel.loadTransportList()
    }

    private fun setupUI() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.currentPage = 1
            viewModel.loadTransportList()
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, NewTransportPriceActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.transportList.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    if (!binding.swipeRefresh.isRefreshing) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    binding.textEmpty.visibility = View.GONE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    val data = result.data?.data ?: emptyList()
                    adapter.submitList(data)
                    binding.textEmpty.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(this, "Błąd: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTransportList()
    }

    override fun performLogout() {
        sessionManager.clear()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}

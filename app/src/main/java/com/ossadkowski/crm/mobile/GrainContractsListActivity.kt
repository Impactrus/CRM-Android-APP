package com.ossadkowski.crm.mobile

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import java.util.*
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityGrainContractsListBinding
import com.ossadkowski.crm.mobile.ui.sales.GrainContractsListAdapter
import com.ossadkowski.crm.mobile.ui.sales.GrainContractsViewModel

class GrainContractsListActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivityGrainContractsListBinding
    private val viewModel: GrainContractsViewModel by viewModels()
    private val adapter = GrainContractsListAdapter { item ->
        val intent = Intent(this, GrainContractDetailActivity::class.java)
        intent.putExtra("CONTRACT_ID", item.id)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrainContractsListBinding.inflate(layoutInflater)
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
        
        viewModel.loadContracts()
    }

    private fun setupUI() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Wyszukiwarka z debounce
        val handler = Handler(Looper.getMainLooper())
        val searchRunnable = Runnable {
            viewModel.currentPage = 1
            viewModel.loadContracts()
        }

        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.search = s?.toString()?.takeIf { it.isNotEmpty() }
                handler.removeCallbacks(searchRunnable)
                handler.postDelayed(searchRunnable, 500)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Statusy
        val statuses = listOf("Wszystkie statusy", "Szkic", "Oczekujący", "Zatwierdzony", "Odrzucony")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = when (position) {
                    1 -> "DRAFT"
                    2 -> "PENDING"
                    3 -> "APPROVED"
                    4 -> "REJECTED"
                    else -> null
                }
                if (viewModel.status != selected) {
                    viewModel.status = selected
                    viewModel.currentPage = 1
                    viewModel.loadContracts()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Daty
        binding.filterDateFrom.setOnClickListener { showDatePicker(true) }
        binding.filterDateTo.setOnClickListener { showDatePicker(false) }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.currentPage = 1
            viewModel.loadContracts()
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    viewModel.loadContracts(isNextPage = true)
                }
            }
        })

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.tab = if (tab?.position == 0) "mine" else "all"
                viewModel.currentPage = 1
                viewModel.loadContracts()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, NewGrainContractActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.contractsList.observe(this) { result ->
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

    private fun showDatePicker(isFrom: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance()
            selected.set(year, month, dayOfMonth)
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selected.time)
            if (isFrom) {
                viewModel.dateFrom = dateStr
                binding.filterDateFrom.text = dateStr
            } else {
                viewModel.dateTo = dateStr
                binding.filterDateTo.text = dateStr
            }
            viewModel.currentPage = 1
            viewModel.loadContracts()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun performLogout() {
        sessionManager.clear()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}

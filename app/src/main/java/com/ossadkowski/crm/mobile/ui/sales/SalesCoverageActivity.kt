package com.ossadkowski.crm.mobile.ui.sales

import android.content.res.ColorStateList
import android.graphics.Color
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
import com.ossadkowski.crm.mobile.BaseDrawerActivity
import com.ossadkowski.crm.mobile.R
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivitySalesCoverageBinding

class SalesCoverageActivity : BaseDrawerActivity() {

    private lateinit var binding: ActivitySalesCoverageBinding
    private val viewModel: SalesCoverageViewModel by viewModels()
    private lateinit var adapter: SalesCoverageAdapter

    private var itemsList: List<com.ossadkowski.crm.mobile.data.model.SalesCoverageItemFacet> = emptyList()
    private var periodsList: List<String> = emptyList()
    private var dkzList: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesCoverageBinding.inflate(layoutInflater)
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

        viewModel.loadFacets()
        viewModel.loadCoverage()
    }

    private fun setupUI() {
        adapter = SalesCoverageAdapter { item ->
            val intent = android.content.Intent(this, SalesCoverageDetailActivity::class.java).apply {
                putExtra("CONTRACT_ID", item.contractNumber)
            }
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Wyszukiwarka z debounce (500ms)
        val handler = Handler(Looper.getMainLooper())
        val searchRunnable = Runnable {
            viewModel.loadCoverage(isNextPage = false)
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

        // Swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadCoverage(isNextPage = false)
        }

        // Paginacja (infinite scroll)
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                    viewModel.loadCoverage(isNextPage = true)
                }
            }
        })

        // Setup Spinner Ryzyka
        val risks = listOf("Wszystkie ryzyka", "Luka", "Brak luki")
        val riskAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, risks)
        riskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRisk.adapter = riskAdapter
        binding.spinnerRisk.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = when (position) {
                    1 -> "risk"
                    2 -> "safe"
                    else -> null
                }
                if (viewModel.risk != selected) {
                    viewModel.risk = selected
                    viewModel.loadCoverage(isNextPage = false)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        // Obserwacja listy pokrycia
        viewModel.coverageList.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.textEmpty.visibility = View.GONE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    val items = result.data?.data ?: emptyList()
                    adapter.submitList(items)
                    if (items.isEmpty()) {
                        binding.textEmpty.visibility = View.VISIBLE
                        binding.textEmpty.text = "Brak danych pokrycia dla wybranych filtrów"
                    } else {
                        binding.textEmpty.visibility = View.GONE
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = "Błąd: ${result.message}"
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Obserwacja słowników (facets)
        viewModel.facets.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val data = result.data
                if (data != null) {
                    setupFacetsSpinners(data)
                }
            }
        }

        // Obserwacja synchronizacji
        viewModel.syncResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    Toast.makeText(this, "Synchronizacja z ERP...", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Success -> {
                    Toast.makeText(this, "Synchronizacja zakończona pomyślnie!", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Błąd synchronizacji: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupFacetsSpinners(facets: com.ossadkowski.crm.mobile.data.model.SalesCoverageFacetsResponse) {
        // 1. Towary
        itemsList = facets.items ?: emptyList()
        val itemLabels = mutableListOf("Wszystkie towary")
        itemLabels.addAll(itemsList.map { it.name ?: "Nieznany" })
        val itemSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, itemLabels)
        itemSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerItem.adapter = itemSpinnerAdapter
        binding.spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedId = if (position > 0) itemsList[position - 1].id else null
                if (viewModel.itemId != selectedId) {
                    viewModel.itemId = selectedId
                    viewModel.loadCoverage(isNextPage = false)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 2. Realizacja (Okresy)
        periodsList = facets.periods ?: emptyList()
        val periodLabels = mutableListOf("Wszystkie realizacje")
        periodLabels.addAll(periodsList)
        val periodSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodLabels)
        periodSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPeriod.adapter = periodSpinnerAdapter
        binding.spinnerPeriod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPeriod = if (position > 0) periodsList[position - 1] else null
                if (viewModel.periodMonth != selectedPeriod) {
                    viewModel.periodMonth = selectedPeriod
                    viewModel.loadCoverage(isNextPage = false)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 3. DKZ
        dkzList = facets.dkzList ?: emptyList()
        val dkzLabels = mutableListOf("Wszystkie DKZ")
        dkzLabels.addAll(dkzList)
        val dkzSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dkzLabels)
        dkzSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDkz.adapter = dkzSpinnerAdapter
        binding.spinnerDkz.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedDkz = if (position > 0) dkzList[position - 1] else null
                if (viewModel.dkz != selectedDkz) {
                    viewModel.dkz = selectedDkz
                    viewModel.loadCoverage(isNextPage = false)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun performLogout() {
        sessionManager.clear()
        startActivity(android.content.Intent(this, com.ossadkowski.crm.mobile.MainActivity::class.java))
        finishAffinity()
    }
}

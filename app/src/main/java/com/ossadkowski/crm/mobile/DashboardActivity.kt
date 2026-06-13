package com.ossadkowski.crm.mobile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.messaging.FirebaseMessaging
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.cache.ActionQueue
import com.ossadkowski.crm.mobile.data.cache.ConnectivityMonitor
import com.ossadkowski.crm.mobile.databinding.ActivityDashboardBinding
import com.ossadkowski.crm.mobile.fcm.DeviceTokenRequest
import com.ossadkowski.crm.mobile.ui.approval.ApprovalAdapter
import com.ossadkowski.crm.mobile.ui.dashboard.DashboardViewModel
import com.ossadkowski.crm.mobile.ui.dashboard.TasksAdapter
import com.ossadkowski.crm.mobile.ui.dashboard.WnioskiAdapter
import com.ossadkowski.crm.mobile.ui.dashboard.BoardColumnAdapter
import com.ossadkowski.crm.mobile.ui.dashboard.ConversationsAdapter
import com.ossadkowski.crm.mobile.util.PaginationHelper
import com.ossadkowski.crm.mobile.util.addDebouncedTextListener
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Button as AndroidButton
import com.ossadkowski.crm.mobile.data.model.SlownikItemDto
import kotlinx.coroutines.*
import android.app.DatePickerDialog
import android.text.Editable
import android.text.TextWatcher
import com.ossadkowski.crm.mobile.ui.serwis.SerwisActivity
import com.ossadkowski.crm.mobile.ui.serwis.access.SerwisAccessChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : BaseDrawerActivity() {

    @Inject lateinit var serwisAccessChecker: SerwisAccessChecker
    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    private val tasksAdapter = TasksAdapter()
    private lateinit var wnioskiAdapter: WnioskiAdapter
    private lateinit var akceptacjeAdapter: ApprovalAdapter
    private lateinit var zastepstwaOczekujaceAdapter: WnioskiAdapter
    private lateinit var zastepstwaZaakceptowaneAdapter: WnioskiAdapter
    private lateinit var boardAdapter: BoardColumnAdapter

    private var activeTab = "zadania"
    private lateinit var connectivityMonitor: ConnectivityMonitor

    private val tasksPagination = PaginationHelper(pageSize = 10) { page ->
        viewModel.tasksPage = page
        viewModel.loadTasks()
    }

    private val wnioskiPagination = PaginationHelper(pageSize = 10) { page ->
        viewModel.wnioskiPage = page
        viewModel.loadWnioski(sessionManager.userId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            binding = ActivityDashboardBinding.inflate(layoutInflater)
            setContentView(binding.root)

        initSession()

        // Evict expired cache + prefetch reference data in background
        CoroutineScope(Dispatchers.IO).launch {
            try { RetrofitClient.cacheDb.evictExpired() } catch (_: Exception) {}
            val prefetchRepo = com.ossadkowski.crm.mobile.data.repository.NewRequestRepository()
            try { prefetchRepo.getTypy() } catch (_: Exception) {}
            try { prefetchRepo.getRodzajeUrlopu() } catch (_: Exception) {}
            try { prefetchRepo.getUzytkownicy() } catch (_: Exception) {}
        }

        wnioskiAdapter = WnioskiAdapter(onClick = { wniosek ->
            val intent = Intent(this, EditRequestActivity::class.java)
            intent.putExtra("id", wniosek.id)
            startActivity(intent)
        }, onSend = { wniosek ->
            viewModel.sendWniosek(wniosek.id, sessionManager.userId) { success, data ->
                runOnUiThread {
                    if (success) {
                        if (data == "queued_offline") {
                            Toast.makeText(this, R.string.saved_offline, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, R.string.wniosek_sent, Toast.LENGTH_SHORT).show()
                            viewModel.loadWnioski(sessionManager.userId)
                        }
                    } else {
                        Toast.makeText(this, R.string.wniosek_send_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        setupProfile()
        setupTabs()
        setupTasksTab()
        setupWnioskiTab()
        setupAkceptacjeTab()
        setupZastepstwaTab()
        setupBoardTab()
        setupPoleceniePracy()
        setupSerwisTile()

        setupDrawer(
            drawerLayout = binding.drawerLayout,
            menuButton = binding.btnMenu,
            drawerClose = binding.includeDrawer.drawerClose,
            drawerName = binding.includeDrawer.drawerName,
            drawerRole = binding.includeDrawer.drawerRole,
            drawerPanel = binding.includeDrawer.drawerPanel,
            drawerApprovals = binding.includeDrawer.drawerApprovals,
            drawerTasks = binding.includeDrawer.drawerTasks,
            drawerCalendar = binding.includeDrawer.drawerCalendar,
            drawerWindykacjaHeader = binding.includeDrawer.drawerWindykacjaHeader,
            drawerWindykacjaArrow = binding.includeDrawer.drawerWindykacjaArrow,
            drawerWindykacjaSub = binding.includeDrawer.drawerWindykacjaSub,
            drawerWindykacjaWnioski = binding.includeDrawer.drawerWindykacjaWnioski,
            drawerWindykacjaNowy = binding.includeDrawer.drawerWindykacjaNowy,
            drawerWindykacjaZadania = binding.includeDrawer.drawerWindykacjaZadania,
            drawerWindykacjaProfil = binding.includeDrawer.drawerWindykacjaProfil,
            drawerHrHeader = binding.includeDrawer.drawerHrHeader,
            drawerHrArrow = binding.includeDrawer.drawerHrArrow,
            drawerHrSub = binding.includeDrawer.drawerHrSub,
            drawerHrAkceptacje = binding.includeDrawer.drawerHrAkceptacje,
            drawerHrHistoria = binding.includeDrawer.drawerHrHistoria,
            drawerHrKalendarz = binding.includeDrawer.drawerHrKalendarz,
            drawerHrNadgodziny = binding.includeDrawer.drawerHrNadgodziny,
            drawerHrHomeOffice = binding.includeDrawer.drawerHrHomeOffice,
            drawerHrPrawoPracy = binding.includeDrawer.drawerHrPrawoPracy,
            drawerHrSchemat = binding.includeDrawer.drawerHrSchemat,
            drawerSalesHeader = binding.includeDrawer.drawerSalesHeader,
            drawerSalesArrow = binding.includeDrawer.drawerSalesArrow,
            drawerSalesSub = binding.includeDrawer.drawerSalesSub,
            drawerSalesOrders = binding.includeDrawer.drawerSalesOrders,
            drawerSalesTransport = binding.includeDrawer.drawerSalesTransport,
            drawerSalesContracts = binding.includeDrawer.drawerSalesContracts,
            drawerSalesReps = binding.includeDrawer.drawerSalesReps,
            drawerSalesClientPanel = binding.includeDrawer.drawerSalesClientPanel,
            drawerSalesClients = binding.includeDrawer.drawerSalesClients,
            drawerSalesGrainTrade = binding.includeDrawer.drawerSalesGrainTrade,
            drawerLogout = binding.includeDrawer.drawerLogout,
            drawerKontrahenci = binding.includeDrawer.drawerKontrahenci,
            drawerTowary = binding.includeDrawer.drawerTowary,
            drawerZamowienia = binding.includeDrawer.drawerZamowienia,
            drawerTransakcje = binding.includeDrawer.drawerTransakcje,
            drawerWizyty = binding.includeDrawer.drawerWizyty,
            drawerOferty = binding.includeDrawer.drawerOferty,
            drawerCrm = binding.includeDrawer.drawerCrm,
            drawerInfo = binding.includeDrawer.drawerInfo,
            drawerMessages = binding.includeDrawer.drawerMessages
        )


        binding.breadcrumb.text = getString(R.string.breadcrumb_dashboard)

        binding.summaryDaysOff.text = "-"
        binding.summaryTasks.text = "-"
        binding.statDaysOff.text = "-"
        binding.statHoRemaining.text = "-"

        binding.btnLogout.setOnClickListener {
            performLogout()
        }

        // Offline action queue monitor
        val actionQueue = ActionQueue(RetrofitClient.cacheDb)
        connectivityMonitor = ConnectivityMonitor(this, actionQueue)
        connectivityMonitor.register()

        viewModel.loadProfile()
        viewModel.loadEmployeeProfile(sessionManager.userId)
        viewModel.loadSaldo()
        selectTab("zadania")
        } catch (e: Exception) {
            android.util.Log.e("DashboardActivity", "CRITICAL CRASH IN ONCREATE", e)
            showCriticalError(e)
        }
    }

    private fun showCriticalError(e: Exception) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Błąd Aplikacji")
            .setMessage("Wystąpił błąd podczas ładowania pulpitu:\n${e.message}\n\nProszę zrobić zrzut ekranu i wysłać do wsparcia.\n\n${e.stackTraceToString().take(500)}...")
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    override fun performLogout() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.apiService.unregisterDeviceToken(DeviceTokenRequest(token))
                } catch (e: Exception) {
                    Log.e("FCM", "Failed to unregister token", e)
                }
            }
        }
        viewModel.logout {
            CoroutineScope(Dispatchers.IO).launch {
                try { RetrofitClient.cacheDb.clearAll() } catch (_: Exception) {}
            }
            runOnUiThread {
                sessionManager.clear()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setupProfile() {
        // Auth profile (for claims and roles)
        viewModel.profile.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val p = result.data ?: return@observe
                
                val apiName = p.name ?: ""
                val apiFName = p.fName ?: ""
                
                if (apiName.isNotBlank() || apiFName.isNotBlank()) {
                    var actualFirstName = if (apiFName.isNotBlank()) apiFName else apiName
                    var actualLastName = if (apiFName.isNotBlank()) apiName else ""
                    
                    // Fallback to extract surname from username (e.g. j_stachowski -> Stachowski)
                    if (actualLastName.isBlank() && sessionManager.username.contains("_")) {
                        val parts = sessionManager.username.split("_")
                        if (parts.size > 1) {
                            actualLastName = parts[1]
                        }
                    }
                    
                    actualFirstName = actualFirstName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    actualLastName = actualLastName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    
                    val fullName = "$actualFirstName $actualLastName".trim().ifBlank { sessionManager.username }
                    sessionManager.updateFullName(fullName)
                    val initial = actualFirstName.firstOrNull()?.toString()?.uppercase() ?: fullName.firstOrNull()?.toString()?.uppercase() ?: "M"

                    binding.profileName.text = fullName
                    binding.profileRole.text = p.workpost ?: p.role ?: sessionManager.role
                    binding.topbarName.text = fullName
                    binding.topbarAvatar.text = initial
                    binding.profileAvatar.text = initial
                    binding.includeDrawer.drawerAvatar.text = initial
                    binding.includeDrawer.drawerName.text = fullName
                    binding.includeDrawer.drawerRole.text = p.workpost ?: p.role ?: sessionManager.role
                }

                // Update claims if needed
                if (p.claims != null && p.claimsVersion != null && p.claimsVersion != sessionManager.claimsVersion) {
                    sessionManager.updateClaims(p.claims, p.claimsVersion)
                }
            }
        }

        // Employee profile (for name, initials, position)
        viewModel.employeeProfile.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val p = result.data ?: return@observe
                val apiName = p.name ?: ""
                val apiFName = p.fName ?: ""
                
                if (apiName.isNotBlank() || apiFName.isNotBlank()) {
                    var actualFirstName = if (apiFName.isNotBlank()) apiFName else apiName
                    var actualLastName = if (apiFName.isNotBlank()) apiName else ""
                    
                    // Fallback to extract surname from username
                    if (actualLastName.isBlank() && sessionManager.username.contains("_")) {
                        val parts = sessionManager.username.split("_")
                        if (parts.size > 1) {
                            actualLastName = parts[1]
                        }
                    }
                    
                    actualFirstName = actualFirstName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    actualLastName = actualLastName.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    
                    val fullName = "$actualFirstName $actualLastName".trim().ifBlank { sessionManager.username }
                    sessionManager.updateFullName(fullName)
                    val initial = actualFirstName.firstOrNull()?.toString()?.uppercase() ?: fullName.firstOrNull()?.toString()?.uppercase() ?: "M"

                    binding.profileName.text = fullName
                    binding.profileRole.text = p.workpost ?: p.role ?: sessionManager.role
                    
                    // topbar
                    binding.topbarName.text = fullName
                    
                    binding.topbarAvatar.text = initial
                    binding.profileAvatar.text = initial
                    binding.includeDrawer.drawerAvatar.text = initial
                    
                    binding.includeDrawer.drawerName.text = fullName
                    binding.includeDrawer.drawerRole.text = p.workpost ?: p.role ?: sessionManager.role
                }
                
                binding.profilePhone.visibility = View.GONE
                binding.profileEmail.visibility = View.VISIBLE
                binding.profileEmail.text = p.email ?: ""
            } else {
                val fallbackName = sessionManager.fullName
                binding.profileName.text = fallbackName
                binding.profileRole.text = sessionManager.role
                binding.topbarName.text = fallbackName.uppercase()
                binding.includeDrawer.drawerName.text = fallbackName
                binding.includeDrawer.drawerRole.text = sessionManager.role
                
                val initial = fallbackName.firstOrNull()?.toString()?.uppercase() ?: "M"
                binding.topbarAvatar.text = initial
                binding.profileAvatar.text = initial
                binding.includeDrawer.drawerAvatar.text = initial
                
                binding.profilePhone.visibility = View.GONE
                binding.profileEmail.visibility = View.GONE
            }
        }

        viewModel.saldo.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val salda = result.data ?: return@observe
                // Logujemy salda dla celów diagnostycznych
                android.util.Log.d("SALDO_DEBUG", "Otrzymano salda: ${salda.map { "${it.kod}: ${it.nazwa}" }}")
            }
        }

        viewModel.homeOfficeSaldo.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val ho = result.data ?: return@observe
                binding.statHoRemaining.text = (ho.saldo ?: 0.0).toInt().toString()
                binding.statHoDetails.text = "Wykorzystane: ${(ho.wykorzystane ?: 0.0).toInt()} • Limit roczny: ${(ho.limit ?: 0.0).toInt()}"
            } else if (result is NetworkResult.Error) {
                binding.statHoRemaining.text = "0"
                binding.statHoDetails.text = "Wykorzystane: 0 • Limit roczny: 0"
            }
        }

        viewModel.overtimeSaldo.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val list = result.data ?: return@observe
                android.util.Log.d("SALDO_DEBUG", "Overtime Saldo count: ${list.size}")
                // Na razie brak widoku dla nadgodzin, ale dane są pobierane
            }
        }

        viewModel.vacationSummary.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val summary = result.data ?: return@observe
                val remaining = summary.remainingDays?.toInt() ?: 0
                val used = summary.usedDays?.toInt() ?: 0
                val previous = summary.previousYearDays?.toInt() ?: 0
                val additional = summary.additionalDays?.toInt() ?: 0
                val total = summary.totalDays?.toInt() ?: 0

                binding.statDaysOff.text = remaining.toString()
                binding.summaryDaysOff.text = remaining.toString()
                
                // Wykorzystane: 5 • Z poprzedniego roku: 11 • Dodatkowe: 0
                val detailsText = "Wykorzystane: $used • Z poprzedniego roku: $previous • Dodatkowe: $additional"
                binding.statDaysOffDetails.text = detailsText
                
                // Wymiar urlopu: 26 dni
                binding.statDaysOffLimit.text = getString(R.string.stat_limit_fmt, total)

                // Obsługa kliknięcia w kafelek
                binding.statDaysOffCard.setOnClickListener {
                    showVacationDetailsDialog(summary)
                }
            }
        }
    }

    private fun showVacationDetailsDialog(summary: com.ossadkowski.crm.mobile.data.model.VacationSummaryDto) {
        val msg = StringBuilder()
        msg.append(getString(R.string.stat_limit_fmt, summary.totalDays?.toInt() ?: 0)).append("\n")
        msg.append(getString(R.string.stat_prev_year_fmt, summary.previousYearDays?.toInt() ?: 0)).append("\n")
        msg.append(getString(R.string.stat_additional_fmt, summary.additionalDays?.toInt() ?: 0)).append("\n")
        msg.append(getString(R.string.stat_used_fmt, summary.usedDays?.toInt() ?: 0)).append("\n\n")
        msg.append("Pozostało do wykorzystania: ${summary.remainingDays?.toInt() ?: 0} dni")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.vacation_details_title)
            .setMessage(msg.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupTabs() {
        val tabViews = listOf(
            binding.tabZadania, binding.tabBoard, binding.tabAkceptacje, 
            binding.tabWnioski, binding.tabZastepstwa, binding.tabPolecenie
        )

        tabViews.forEach { tv ->
            tv.setOnClickListener {
                val tab = when (tv.id) {
                    R.id.tab_zadania -> "zadania"
                    R.id.tab_board -> "board"
                    R.id.tab_wnioski -> "wnioski"
                    R.id.tab_zastepstwa -> "zastepstwa"
                    R.id.tab_polecenie -> "polecenie"
                    R.id.tab_akceptacje -> "akceptacje"
                    else -> "zadania"
                }
                selectTab(tab)
            }
        }
    }

    private fun selectTab(tab: String) {
        activeTab = tab
        val tabs = listOf(
            binding.tabZadania to "zadania",
            binding.tabBoard to "board",
            binding.tabAkceptacje to "akceptacje",
            binding.tabWnioski to "wnioski",
            binding.tabZastepstwa to "zastepstwa",
            binding.tabPolecenie to "polecenie"
        )

        tabs.forEach { (tv, name) ->
            if (name == tab) {
                tv.setBackgroundResource(R.drawable.bg_tab_active)
                tv.setTextColor(android.graphics.Color.parseColor("#374151"))
                (tv as TextView).typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
            } else {
                tv.setBackgroundResource(R.drawable.bg_tab_inactive)
                tv.setTextColor(android.graphics.Color.parseColor("#6B7280"))
                (tv as TextView).typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
            }
        }

        binding.tasksContainer.visibility = if (tab == "zadania") View.VISIBLE else View.GONE
        binding.boardContainer.visibility = if (tab == "board") View.VISIBLE else View.GONE
        binding.wnioskiContainer.visibility = if (tab == "wnioski") View.VISIBLE else View.GONE
        binding.zastepstwaContainer.visibility = if (tab == "zastepstwa") View.VISIBLE else View.GONE
        binding.polecenieContainer.visibility = if (tab == "polecenie") View.VISIBLE else View.GONE
        binding.akceptacjeContainer.visibility = if (tab == "akceptacje") View.VISIBLE else View.GONE

        when (tab) {
            "zadania" -> viewModel.loadTasks()
            "board" -> viewModel.loadBoard()
            "wnioski" -> viewModel.loadWnioski(sessionManager.userId)
            "zastepstwa" -> viewModel.loadZastepstwa()
            "akceptacje" -> viewModel.loadApprovals(sessionManager.userId, sessionManager.approvalRole)
            "polecenie" -> setupPoleceniePracy()
        }
    }

    private fun setupAkceptacjeTab() {
        akceptacjeAdapter = ApprovalAdapter(
            onApprove = { wniosek ->
                // Szybka akcja z dashboardu
            },
            onReject = { wniosek ->
                // Szybka akcja z dashboardu
            },
            onItemClick = { wniosek ->
                val intent = Intent(this, ApprovalDetailActivity::class.java).apply {
                    putExtra("wniosek_id", wniosek.id)
                    putExtra("wniosek_num", wniosek.id.toString())
                }
                startActivity(intent)
            }
        )

        binding.akceptacjeRecycler.layoutManager = LinearLayoutManager(this)
        binding.akceptacjeRecycler.adapter = akceptacjeAdapter

        viewModel.approvals.observe(this) { result ->
            binding.akceptacjeProgress.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.akceptacjeProgress.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    akceptacjeAdapter.submitList(data.items)
                    binding.akceptacjeEmptyText.visibility = if (data.items.isEmpty()) View.VISIBLE else View.GONE
                }
                is NetworkResult.Error -> {
                    if (result.message?.contains("401") != true) {
                        Toast.makeText(this, "Błąd: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }



    private fun setupTasksTab() {
        binding.tasksRecycler.layoutManager = LinearLayoutManager(this)
        binding.tasksRecycler.setHasFixedSize(false)
        binding.tasksRecycler.adapter = tasksAdapter

        binding.tasksSearch.addDebouncedTextListener(lifecycleScope) { query ->
            viewModel.tasksSearch = query.takeIf { it.isNotBlank() }
            viewModel.tasksPage = 1
            tasksPagination.reset()
            viewModel.loadTasks()
        }

        viewModel.tasks.observe(this) { result ->
            binding.tasksProgress.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.tasksProgress.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    tasksAdapter.submitList(data.items)
                    binding.summaryTasks.text = data.totalCount.toString()
                    tasksPagination.updateFromResponse(data.totalCount, data.totalPages)
                    binding.tasksBtnPrev.isEnabled = tasksPagination.hasPrev()
                    binding.tasksBtnNext.isEnabled = tasksPagination.hasNext()
                }
                is NetworkResult.Error -> {
                    if (result.message?.contains("401") != true) {
                        Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.tasksBtnPrev.setOnClickListener { tasksPagination.prevPage() }
        binding.tasksBtnNext.setOnClickListener { tasksPagination.nextPage() }
    }

    private fun setupWnioskiTab() {
        binding.wnioskiRecycler.layoutManager = LinearLayoutManager(this)
        binding.wnioskiRecycler.setHasFixedSize(false)
        binding.wnioskiRecycler.adapter = wnioskiAdapter

        binding.btnNewWniosek.setOnClickListener {
            startActivity(Intent(this, NewRequestActivity::class.java))
        }

        viewModel.wnioski.observe(this) { result ->
            binding.wnioskiProgress.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.wnioskiProgress.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    wnioskiAdapter.submitList(data.items)
                    wnioskiPagination.updateFromResponse(data.totalCount, data.totalPages)
                    binding.wnioskiBtnPrev.isEnabled = wnioskiPagination.hasPrev()
                    binding.wnioskiBtnNext.isEnabled = wnioskiPagination.hasNext()
                }
                is NetworkResult.Error -> {
                    if (result.message?.contains("401") != true) {
                        Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.wnioskiBtnPrev.setOnClickListener { wnioskiPagination.prevPage() }
        binding.wnioskiBtnNext.setOnClickListener { wnioskiPagination.nextPage() }
    }

    override fun onResume() {
        super.onResume()
        if (activeTab == "wnioski") viewModel.loadWnioski(sessionManager.userId)
        // Process any pending offline actions
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val actionQueue = ActionQueue(RetrofitClient.cacheDb)
                val count = actionQueue.processAll()
                if (count > 0) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DashboardActivity, getString(R.string.pending_actions_processed, count), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::connectivityMonitor.isInitialized) connectivityMonitor.unregister()
    }
    private fun setupPoleceniePracy() {
        binding.btnPolecenieSobota.setOnClickListener {
            showPoleceniePracyDialog("Sobota")
        }
        binding.btnPolecenieNiedziela.setOnClickListener {
            showPoleceniePracyDialog("Niedziela")
        }

        // Observer for creation status (only once)
        viewModel.createPolecenieStatus.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    Toast.makeText(this, "Polecenie pracy zostało wystawione pomyślnie!", Toast.LENGTH_LONG).show()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Błąd wystawiania polecenia: ${result.message}", Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun showPoleceniePracyDialog(dzien: String) {
        viewModel.loadUzytkownicy()

        val dialogView = layoutInflater.inflate(R.layout.dialog_polecenie_pracy, null)
        val titleTv = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val workerSpinner = dialogView.findViewById<Spinner>(R.id.workerSpinner)
        val dateInput = dialogView.findViewById<EditText>(R.id.dateInput)
        val reasonInput = dialogView.findViewById<EditText>(R.id.reasonInput)
        val btnSubmit = dialogView.findViewById<AndroidButton>(R.id.btnSubmit)
        val btnCancel = dialogView.findViewById<AndroidButton>(R.id.btnCancel)

        titleTv.text = "Polecenie pracy w $dzien"

        // Oblicz domyślną datę
        val cal = java.util.Calendar.getInstance()
        val targetDay = if (dzien == "Sobota") java.util.Calendar.SATURDAY else java.util.Calendar.SUNDAY
        while (cal.get(java.util.Calendar.DAY_OF_WEEK) != targetDay) {
            cal.add(java.util.Calendar.DATE, 1)
        }
        var selectedYear = cal.get(java.util.Calendar.YEAR)
        var selectedMonth = cal.get(java.util.Calendar.MONTH)
        var selectedDay = cal.get(java.util.Calendar.DAY_OF_MONTH)

        fun updateDateLabel() {
            val d = if (selectedDay < 10) "0$selectedDay" else "$selectedDay"
            val mNum = selectedMonth + 1
            val m = if (mNum < 10) "0$mNum" else "$mNum"
            dateInput.setText("$d.$m.$selectedYear")
        }
        updateDateLabel()

        dateInput.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                selectedYear = y
                selectedMonth = m
                selectedDay = d
                updateDateLabel()
            }, selectedYear, selectedMonth, selectedDay).show()
        }

        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        viewModel.uzytkownicy.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val list = result.data ?: emptyList()
                val names = list.map { it.nazwa }.toMutableList()
                names.add(0, "— Wybierz pracownika —")
                
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names)
                workerSpinner.adapter = adapter
            }
        }

        val validate = {
            val isWorkerSelected = workerSpinner.selectedItemPosition > 0
            val isReasonOk = (reasonInput.text?.length ?: 0) >= 10
            btnSubmit.isEnabled = isWorkerSelected && isReasonOk
        }

        reasonInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validate() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Spinner doesn't have a simple text watcher, so we use item selected
        workerSpinner.post {
            workerSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: android.widget.AdapterView<*>?, p1: View?, p2: Int, p3: Long) { validate() }
                override fun onNothingSelected(p0: android.widget.AdapterView<*>?) {}
            }
        }

        btnCancel.setOnClickListener { alertDialog.dismiss() }

        btnSubmit.setOnClickListener {
            val workerPos = workerSpinner.selectedItemPosition
            if (workerPos > 0) {
                val worker = (viewModel.uzytkownicy.value as? NetworkResult.Success)?.data?.get(workerPos - 1)
                if (worker != null) {
                    val d = if (selectedDay < 10) "0$selectedDay" else "$selectedDay"
                    val mNum = selectedMonth + 1
                    val m = if (mNum < 10) "0$mNum" else "$mNum"
                    val isoDate = "$selectedYear-$m-$d"
                    
                    viewModel.createPoleceniePracy(sessionManager.userId, worker.id, isoDate, dzien)
                    alertDialog.dismiss()
                }
            }
        }

        alertDialog.show()
    }

    private fun setupZastepstwaTab() {
        zastepstwaOczekujaceAdapter = WnioskiAdapter(onClick = { wniosek ->
            val intent = Intent(this, EditRequestActivity::class.java)
            intent.putExtra("id", wniosek.id)
            startActivity(intent)
        })

        zastepstwaZaakceptowaneAdapter = WnioskiAdapter(onClick = { wniosek ->
            val intent = Intent(this, EditRequestActivity::class.java)
            intent.putExtra("id", wniosek.id)
            startActivity(intent)
        })

        binding.zastepstwaOczekujaceRecycler.layoutManager = LinearLayoutManager(this)
        binding.zastepstwaOczekujaceRecycler.adapter = zastepstwaOczekujaceAdapter

        binding.zastepstwaZaakceptowaneRecycler.layoutManager = LinearLayoutManager(this)
        binding.zastepstwaZaakceptowaneRecycler.adapter = zastepstwaZaakceptowaneAdapter

        // Oczekujące
        viewModel.zastepstwaOczekujace.observe(this) { result ->
            binding.zastepstwaOczekujaceProgress.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.zastepstwaOczekujaceProgress.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    zastepstwaOczekujaceAdapter.submitList(data)
                    binding.zastepstwaOczekujaceEmptyText.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Błąd zastępstw oczekujących: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Zaakceptowane
        viewModel.zastepstwaZaakceptowane.observe(this) { result ->
            binding.zastepstwaZaakceptowaneProgress.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.zastepstwaZaakceptowaneProgress.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    zastepstwaZaakceptowaneAdapter.submitList(data)
                    binding.zastepstwaZaakceptowaneEmptyText.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Błąd zastępstw zaakceptowanych: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Show the Serwis (field-service) entry tile only for users whose role / claims
     * include the field-service module — see [SerwisAccessChecker].
     */
    private fun setupSerwisTile() {
        val tile = binding.tileSerwis
        if (serwisAccessChecker.hasAccess()) {
            tile.visibility = View.VISIBLE
            tile.setOnClickListener {
                startActivity(Intent(this, SerwisActivity::class.java))
            }
        } else {
            tile.visibility = View.GONE
        }
    }

    private fun setupBoardTab() {
        boardAdapter = BoardColumnAdapter(onTaskClick = { task ->
            val intent = Intent(this, EditRequestActivity::class.java)
            intent.putExtra("id", task.id)
            startActivity(intent)
        })

        binding.boardRecycler.layoutManager = LinearLayoutManager(this)
        binding.boardRecycler.adapter = boardAdapter

        viewModel.boardTasks.observe(this) { result ->
            binding.boardProgress.visibility = View.GONE
            when (result) {
                is NetworkResult.Loading -> binding.boardProgress.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    val data = result.data ?: return@observe
                    boardAdapter.submitList(data.columns.toList())
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, "Błąd Boarda: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Filtry
        val updateFilters = { active: String ->
            binding.btnBoardFilterMy.setBackgroundResource(if (active == "moje") R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.btnBoardFilterTeam.setBackgroundResource(if (active == "zespol") R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
            binding.btnBoardFilterAll.setBackgroundResource(if (active == "wszystkie") R.drawable.bg_tab_active else R.drawable.bg_tab_inactive)
        }

        binding.btnBoardFilterMy.setOnClickListener {
            updateFilters("moje")
            viewModel.loadBoard("moje")
        }
        binding.btnBoardFilterTeam.setOnClickListener {
            updateFilters("zespol")
            viewModel.loadBoard("zespol")
        }
        binding.btnBoardFilterAll.setOnClickListener {
            updateFilters("wszystkie")
            viewModel.loadBoard("wszystkie")
        }
    }

}


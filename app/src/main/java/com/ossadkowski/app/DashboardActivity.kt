package com.ossadkowski.app

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
import com.ossadkowski.app.data.NetworkResult
import com.ossadkowski.app.data.api.RetrofitClient
import com.ossadkowski.app.data.cache.ActionQueue
import com.ossadkowski.app.data.cache.ConnectivityMonitor
import com.ossadkowski.app.databinding.ActivityDashboardBinding
import com.ossadkowski.app.fcm.DeviceTokenRequest
import com.ossadkowski.app.ui.dashboard.DashboardViewModel
import com.ossadkowski.app.ui.dashboard.TasksAdapter
import com.ossadkowski.app.ui.dashboard.WnioskiAdapter
import com.ossadkowski.app.util.PaginationHelper
import com.ossadkowski.app.util.addDebouncedTextListener
import kotlinx.coroutines.*

class DashboardActivity : BaseDrawerActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    private val tasksAdapter = TasksAdapter()
    private lateinit var wnioskiAdapter: WnioskiAdapter

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
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSession()

        // Evict expired cache + prefetch reference data in background
        CoroutineScope(Dispatchers.IO).launch {
            try { RetrofitClient.cacheDb.evictExpired() } catch (_: Exception) {}
            val prefetchRepo = com.ossadkowski.app.data.repository.NewRequestRepository()
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

        setupDrawer(
            drawerLayout = binding.drawerLayout,
            menuButton = binding.btnMenu,
            drawerClose = binding.drawerClose,
            drawerName = binding.drawerName,
            drawerRole = binding.drawerRole,
            drawerPanel = binding.drawerPanel,
            drawerApprovals = binding.drawerApprovals,
            drawerTasks = binding.drawerTasks,
            drawerCalendar = binding.drawerCalendar,
            drawerLimityKredytowe = binding.drawerLimityKredytowe,
            drawerLogout = binding.drawerLogout,
            drawerKontrahenci = binding.drawerKontrahenci,
            drawerTowary = binding.drawerTowary,
            drawerZamowienia = binding.drawerZamowienia,
            drawerTransakcje = binding.drawerTransakcje,
            drawerWizyty = binding.drawerWizyty,
            drawerOferty = binding.drawerOferty,
            drawerCrm = binding.drawerCrm,
            drawerInfo = binding.drawerInfo
        )

        binding.btnLogout.setOnClickListener { performLogout() }

        binding.breadcrumb.text = getString(R.string.breadcrumb_dashboard)

        binding.statOrders.text = "-"
        binding.statClients.text = "-"
        binding.statDaysOff.text = "-"
        binding.statPlanned.text = "-"

        // Offline action queue monitor
        val actionQueue = ActionQueue(RetrofitClient.cacheDb)
        connectivityMonitor = ConnectivityMonitor(this, actionQueue)
        connectivityMonitor.register()

        viewModel.loadProfile()
        selectTab("zadania")
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
        viewModel.profile.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val p = result.data ?: return@observe
                val fullName = p.username ?: sessionManager.username
                binding.profileName.text = fullName
                binding.profileRole.text = p.role ?: sessionManager.role
                binding.topbarName.text = fullName.split(" ").let {
                    if (it.size > 1) "${it[0]} ${it[1].first()}." else it[0]
                }
                binding.drawerName.text = fullName
                binding.drawerRole.text = p.role ?: sessionManager.role
                binding.profilePhone.visibility = View.GONE
                binding.profileEmail.visibility = View.GONE
                // Refresh claims from profile if version changed
                if (p.claims != null && p.claimsVersion != null && p.claimsVersion != sessionManager.claimsVersion) {
                    sessionManager.updateClaims(p.claims, p.claimsVersion)
                }
            } else {
                binding.profileName.text = sessionManager.username
                binding.profileRole.text = sessionManager.role
                binding.topbarName.text = sessionManager.username
                binding.drawerName.text = sessionManager.username
                binding.drawerRole.text = sessionManager.role
                binding.profilePhone.visibility = View.GONE
                binding.profileEmail.visibility = View.GONE
            }
        }
    }

    private fun setupTabs() {
        binding.tabZadania.setOnClickListener { selectTab("zadania") }
        binding.tabWnioski.setOnClickListener { selectTab("wnioski") }
    }

    private fun selectTab(tab: String) {
        activeTab = tab
        val tabs = listOf(
            binding.tabZadania to "zadania",
            binding.tabWnioski to "wnioski"
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
        binding.wnioskiContainer.visibility = if (tab == "wnioski") View.VISIBLE else View.GONE

        when (tab) {
            "zadania" -> viewModel.loadTasks()
            "wnioski" -> viewModel.loadWnioski(sessionManager.userId)
        }
    }

    private fun setupTasksTab() {
        binding.tasksRecycler.layoutManager = LinearLayoutManager(this)
        binding.tasksRecycler.setHasFixedSize(true)
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
                    tasksPagination.updateFromResponse(data.totalCount, data.totalPages)
                    binding.tasksPageInfo.text = getString(R.string.page_info_showing, tasksPagination.getShowingStart(), tasksPagination.getShowingEnd(), tasksPagination.totalCount)
                    binding.tasksBtnPrev.isEnabled = tasksPagination.hasPrev()
                    binding.tasksBtnNext.isEnabled = tasksPagination.hasNext()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
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
                    binding.wnioskiPageInfo.text = getString(R.string.page_info_showing, wnioskiPagination.getShowingStart(), wnioskiPagination.getShowingEnd(), wnioskiPagination.totalCount)
                    binding.wnioskiBtnPrev.isEnabled = wnioskiPagination.hasPrev()
                    binding.wnioskiBtnNext.isEnabled = wnioskiPagination.hasNext()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
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
}

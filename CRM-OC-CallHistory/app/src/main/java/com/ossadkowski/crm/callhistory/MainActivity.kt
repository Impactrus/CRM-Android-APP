package com.ossadkowski.crm.callhistory

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.net.Uri
import com.google.android.gms.location.*
import com.ossadkowski.crm.callhistory.databinding.ActivityMainBinding
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val callAdapter = CallAdapter()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskManager: TaskManager
    
    // Organizer Properties
    private lateinit var organizerManager: OrganizerManager
    private lateinit var organizerAdapter: OrganizerAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    
    // GPS / Map Properties
    private var mapView: MapView? = null
    private val dwellTracker = HashMap<String, Long>() // clientId -> first entry timestamp
    
    private var allCalls = listOf<CallItem>()
    private var activeFilter = "all"
    private var searchQuery = ""
    private var activeTab = "history"

    // References to the currently open add-task dialog fields
    private var dialogNameEdit: EditText? = null
    private var dialogPhoneEdit: EditText? = null

    private val contactPickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val contactUri = result.data?.data ?: return@registerForActivityResult
            val projection = arrayOf(
                android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
                android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            )
            try {
                contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val numberIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        val number = cursor.getString(numberIndex)
                        val name = cursor.getString(nameIndex)
                        
                        dialogPhoneEdit?.setText(number)
                        dialogNameEdit?.setText(name)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Błąd odczytu kontaktu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val refreshTasksReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            runOnUiThread {
                loadTasks()
                loadCalls()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val NOTIFICATION_CHANNEL_ID = "organizer_channel"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taskManager = TaskManager(this)
        organizerManager = OrganizerManager(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // OSMDroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid_prefs", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName

        createNotificationChannel()
        setupRecyclerViews()
        setupFilters()
        setupSearch()
        setupNavigation()
        setupMap()

        binding.btnGrant.setOnClickListener {
            requestPermissions()
        }

        binding.btnSettings.setOnClickListener {
            openAppSettings()
        }

        binding.btnTopbarSettings.setOnClickListener {
            openAppSettings()
        }

        binding.btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.btnSyncCrm.setOnClickListener {
            syncFromCrm()
        }

        binding.btnSyncGps.setOnClickListener {
            syncGpsContractors()
        }

        checkAndLoad()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra("CLIENT_ID")) {
            val clientId = intent.getStringExtra("CLIENT_ID") ?: return
            // If coming from notification, open visit panel
            if (intent.getBooleanExtra("OPEN_VISIT_PANEL", false)) {
                val items = organizerManager.getItems()
                val client = items.find { it.id == clientId }
                if (client != null) {
                    openVisitPanel(client)
                }
            } else {
                switchTab("organizer")
                val items = organizerManager.getItems()
                val client = items.find { it.id == clientId }
                if (client != null) {
                    showAddNoteDialog(client)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter("com.ossadkowski.crm.callhistory.REFRESH_TASKS")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(refreshTasksReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(refreshTasksReceiver, filter)
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(refreshTasksReceiver)
        stopLocationTracking()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        if (hasPermissions()) {
            checkRecentCallsForTasks()
            loadCalls()
            loadTasks()
            loadOrganizer()
            startLocationTracking()
        } else {
            requestPermissions()
        }
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    private fun checkAndLoad() {
        if (hasPermissions()) {
            binding.permissionCard.visibility = View.GONE
            binding.statsContainer.visibility = View.VISIBLE
            binding.searchBar.visibility = View.VISIBLE
            switchTab(activeTab)
        } else {
            binding.permissionCard.visibility = View.VISIBLE
            binding.statsContainer.visibility = View.GONE
            binding.searchBar.visibility = View.GONE
            binding.emptyText.visibility = View.GONE
        }
    }

    private fun hasPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                checkAndLoad()
                startLocationTracking()
            } else {
                Toast.makeText(this, "Wymagane są uprawnienia do działania aplikacji", Toast.LENGTH_LONG).show()
                checkAndLoad()
            }
        }
    }

    private fun setupRecyclerViews() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = callAdapter

        taskAdapter = TaskAdapter(
            onDeleteClick = { task ->
                taskManager.deleteTask(task.id)
                loadTasks()
            },
            onCallClick = { task ->
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${task.phoneNumber}"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Nie można otworzyć dialera", Toast.LENGTH_SHORT).show()
                }
            }
        )
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.tasksRecyclerView.adapter = taskAdapter

        // Setup Organizer Adapter
        organizerAdapter = OrganizerAdapter(
            onSimulateClick = { item ->
                simulatePresenceNotification(item)
            },
            onAddNoteClick = { item ->
                showAddNoteDialog(item)
            }
        )
        binding.organizerRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.organizerRecyclerView.adapter = organizerAdapter
    }

    private fun setupNavigation() {
        binding.navHistory.setOnClickListener {
            switchTab("history")
        }
        binding.navTasks.setOnClickListener {
            switchTab("tasks")
        }
        binding.navOrganizer.setOnClickListener {
            switchTab("organizer")
        }
        binding.navGps.setOnClickListener {
            switchTab("gps")
        }
    }

    private fun switchTab(tab: String) {
        activeTab = tab
        
        binding.layoutHistoryContainer.visibility = if (tab == "history") View.VISIBLE else View.GONE
        binding.layoutTasksContainer.visibility = if (tab == "tasks") View.VISIBLE else View.GONE
        binding.layoutOrganizerContainer.visibility = if (tab == "organizer") View.VISIBLE else View.GONE
        binding.layoutGpsContainer.visibility = if (tab == "gps") View.VISIBLE else View.GONE

        // History Tab Styling
        binding.navHistoryIcon.setColorFilter(
            ContextCompat.getColor(this, if (tab == "history") R.color.crm_primary else R.color.crm_placeholder)
        )
        binding.navHistoryText.setTextColor(
            ContextCompat.getColor(this, if (tab == "history") R.color.crm_primary else R.color.crm_placeholder)
        )
        
        // Tasks Tab Styling
        binding.navTasksIcon.setColorFilter(
            ContextCompat.getColor(this, if (tab == "tasks") R.color.crm_primary else R.color.crm_placeholder)
        )
        binding.navTasksText.setTextColor(
            ContextCompat.getColor(this, if (tab == "tasks") R.color.crm_primary else R.color.crm_placeholder)
        )

        // Organizer Tab Styling
        binding.navOrganizerIcon.setColorFilter(
            ContextCompat.getColor(this, if (tab == "organizer") R.color.crm_primary else R.color.crm_placeholder)
        )
        binding.navOrganizerText.setTextColor(
            ContextCompat.getColor(this, if (tab == "organizer") R.color.crm_primary else R.color.crm_placeholder)
        )

        // GPS Tab Styling
        binding.navGpsIcon.setColorFilter(
            ContextCompat.getColor(this, if (tab == "gps") R.color.crm_primary else R.color.crm_placeholder)
        )
        binding.navGpsText.setTextColor(
            ContextCompat.getColor(this, if (tab == "gps") R.color.crm_primary else R.color.crm_placeholder)
        )

        when (tab) {
            "history" -> loadCalls()
            "tasks" -> loadTasks()
            "organizer" -> loadOrganizer()
            "gps" -> loadGpsMarkers()
        }
    }

    private fun loadCalls() {
        if (!hasPermissions()) return
        binding.progressBar.visibility = View.VISIBLE
        Thread {
            val callsList = mutableListOf<CallItem>()
            val projection = arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE
            )

            try {
                val cursor: Cursor? = contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC"
                )

                cursor?.use {
                    val idCol = it.getColumnIndexOrThrow(CallLog.Calls._ID)
                    val numCol = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                    val nameCol = it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                    val typeCol = it.getColumnIndexOrThrow(CallLog.Calls.TYPE)
                    val durCol = it.getColumnIndexOrThrow(CallLog.Calls.DURATION)
                    val dateCol = it.getColumnIndexOrThrow(CallLog.Calls.DATE)

                    while (it.moveToNext()) {
                        val id = it.getString(idCol)
                        val number = it.getString(numCol) ?: ""
                        val name = it.getString(nameCol)
                        val type = it.getInt(typeCol)
                        val duration = it.getLong(durCol)
                        val date = it.getLong(dateCol)

                        callsList.add(CallItem(id, name, number, type, duration, date))
                    }
                }
            } catch (e: SecurityException) {
                runOnUiThread {
                    Toast.makeText(this, "Błąd zabezpieczeń przy czytaniu rejestru", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Błąd wczytywania połączeń: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            runOnUiThread {
                allCalls = callsList
                binding.progressBar.visibility = View.GONE
                calculateStats()
                applyFiltersAndSearch()
            }
        }.start()
    }

    private fun loadTasks() {
        val tasks = taskManager.getTasks()
        taskAdapter.submitList(tasks.reversed())

        val pending = tasks.count { !it.isCompleted }
        val completed = tasks.count { it.isCompleted }

        binding.statTasksPending.text = pending.toString()
        binding.statTasksCompleted.text = completed.toString()
        
        binding.tasksEmptyText.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun checkRecentCallsForTasks() {
        if (!hasPermissions()) return
        Thread {
            val projection = arrayOf(
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME
            )
            try {
                val cursor: Cursor? = contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${CallLog.Calls.DATE} DESC LIMIT 15"
                )

                var hasUpdates = false
                cursor?.use {
                    val numCol = it.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
                    val nameCol = it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
                    while (it.moveToNext()) {
                        val number = it.getString(numCol) ?: ""
                        val name = it.getString(nameCol) ?: ""
                        if (number.isNotBlank()) {
                            val tasks = taskManager.getTasks()
                            val cleanNumber = number.filter { c -> c.isDigit() }
                            val hasMatchingActiveTask = tasks.any { t -> 
                                !t.isCompleted && t.phoneNumber.filter { c -> c.isDigit() }.takeLast(9) == cleanNumber.takeLast(9) 
                            }
                            if (hasMatchingActiveTask) {
                                val completed = taskManager.checkAndCompleteTask(number, name)
                                if (completed) {
                                    hasUpdates = true
                                }
                            }
                        }
                    }
                }
                if (hasUpdates) {
                    runOnUiThread {
                        loadTasks()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun calculateStats() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayMs = today.timeInMillis

        val todayCalls = allCalls.filter { it.timestamp >= todayMs }
        val totalToday = todayCalls.size
        val missedToday = todayCalls.count { it.type == CallLog.Calls.MISSED_TYPE || it.type == CallLog.Calls.REJECTED_TYPE }

        binding.statTotalCount.text = totalToday.toString()
        binding.statMissedCount.text = missedToday.toString()
    }

    private fun setupFilters() {
        val tabViews = listOf(
            binding.tabAll to "all",
            binding.tabIncoming to "incoming",
            binding.tabOutgoing to "outgoing",
            binding.tabMissed to "missed"
        )

        tabViews.forEach { (tv, filterName) ->
            tv.setOnClickListener {
                selectFilter(filterName)
            }
        }
    }

    private fun selectFilter(filterName: String) {
        activeFilter = filterName
        val tabViews = listOf(
            binding.tabAll to "all",
            binding.tabIncoming to "incoming",
            binding.tabOutgoing to "outgoing",
            binding.tabMissed to "missed"
        )

        tabViews.forEach { (tv, name) ->
            if (name == filterName) {
                tv.setBackgroundResource(R.drawable.bg_tab_active)
                tv.setTextColor(ContextCompat.getColor(this, R.color.crm_heading))
            } else {
                tv.setBackgroundResource(R.drawable.bg_tab_inactive)
                tv.setTextColor(ContextCompat.getColor(this, R.color.crm_secondary))
            }
        }

        applyFiltersAndSearch()
    }

    private fun setupSearch() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString()?.trim() ?: ""
                applyFiltersAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFiltersAndSearch() {
        var filteredList = allCalls

        filteredList = when (activeFilter) {
            "incoming" -> filteredList.filter { it.type == CallLog.Calls.INCOMING_TYPE }
            "outgoing" -> filteredList.filter { it.type == CallLog.Calls.OUTGOING_TYPE }
            "missed" -> filteredList.filter { it.type == CallLog.Calls.MISSED_TYPE || it.type == CallLog.Calls.REJECTED_TYPE }
            else -> filteredList
        }

        if (searchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                val nameMatch = it.name?.contains(searchQuery, ignoreCase = true) == true
                val numberMatch = it.number.contains(searchQuery, ignoreCase = true)
                nameMatch || numberMatch
            }
        }

        callAdapter.submitList(filteredList)
        binding.emptyText.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showAddTaskDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Dodaj zadanie")

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etName = view.findViewById<EditText>(R.id.et_task_contact_name)
        val etPhone = view.findViewById<EditText>(R.id.et_task_phone)
        val etTitle = view.findViewById<EditText>(R.id.et_task_title)
        val btnContacts = view.findViewById<View>(R.id.btn_pick_contacts)
        val btnRecent = view.findViewById<View>(R.id.btn_pick_recent)

        dialogNameEdit = etName
        dialogPhoneEdit = etPhone

        btnContacts.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            contactPickerLauncher.launch(intent)
        }

        btnRecent.setOnClickListener {
            showRecentCallsPickerDialog()
        }

        builder.setView(view)
        builder.setPositiveButton("Dodaj") { dialog, _ ->
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val title = etTitle.text.toString().trim()

            if (phone.isEmpty() || title.isEmpty()) {
                Toast.makeText(this, "Numer telefonu i opis są wymagane", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            taskManager.addTask(name, phone, title)
            loadTasks()
            dialog.dismiss()
        }
        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.setOnDismissListener {
            dialogNameEdit = null
            dialogPhoneEdit = null
        }
        dialog.show()
    }

    private fun showRecentCallsPickerDialog() {
        val uniqueRecentCalls = allCalls.distinctBy { it.number.filter { c -> c.isDigit() }.takeLast(9) }.take(15)
        if (uniqueRecentCalls.isEmpty()) {
            Toast.makeText(this, "Brak ostatnich połączeń na liście", Toast.LENGTH_SHORT).show()
            return
        }

        val displayItems = uniqueRecentCalls.map {
            if (!it.name.isNullOrBlank()) "${it.name} (${it.number})" else it.number
        }.toTypedArray()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wybierz z ostatnich połączeń")
        builder.setItems(displayItems) { _, which ->
            val selectedCall = uniqueRecentCalls[which]
            dialogPhoneEdit?.setText(selectedCall.number)
            if (!selectedCall.name.isNullOrBlank()) {
                dialogNameEdit?.setText(selectedCall.name)
            } else {
                dialogNameEdit?.setText("")
            }
        }
        builder.create().show()
    }

    // ── ORGANIZER IMPLEMENTATION ──

    private fun loadOrganizer() {
        val items = organizerManager.getItems()
        organizerAdapter.submitList(items)
        
        binding.organizerEmptyText.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun syncFromCrm() {
        val query = binding.etKontrahentSearchInput.text.toString().trim()
        if (query.length < 3) {
            Toast.makeText(this, "Wpisz co najmniej 3 znaki do wyszukania", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "Wyszukiwanie w CRM...", Toast.LENGTH_SHORT).show()
        CrmApiSync.syncAddressBook(
            context = this,
            searchQuery = query,
            onSuccess = { responseJson ->
                runOnUiThread {
                    organizerManager.syncFromAddressBook(responseJson)
                    loadOrganizer()
                    Toast.makeText(this, "Pobrano dane kontrahentów", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { errorMessage ->
                runOnUiThread {
                    Toast.makeText(this, "Błąd: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun showAddNoteDialog(item: OrganizerItem) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Wizyta u: ${item.name}")
        
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        val etName = view.findViewById<EditText>(R.id.et_task_contact_name)
        val etPhone = view.findViewById<EditText>(R.id.et_task_phone)
        val etTitle = view.findViewById<EditText>(R.id.et_task_title)
        
        // Adapt dialog views for Organizer note instead of Task creation
        view.findViewById<View>(R.id.btn_pick_contacts).visibility = View.GONE
        view.findViewById<View>(R.id.btn_pick_recent).visibility = View.GONE
        
        etName.setText(item.address)
        etName.isEnabled = false
        etPhone.setText("Adres kontrahenta")
        etPhone.isEnabled = false
        
        etTitle.setHint("Opisz jak przebiegła wizyta...")
        etTitle.setText(item.lastVisitNote)

        builder.setView(view)
        builder.setPositiveButton("Zapisz") { dialog, _ ->
            val note = etTitle.text.toString().trim()
            organizerManager.updateNote(item.id, note)
            loadOrganizer()
            dialog.dismiss()
        }
        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun openVisitPanel(item: OrganizerItem) {
        val intent = Intent(this, VisitPanelActivity::class.java).apply {
            putExtra("CLIENT_ID", item.id)
            putExtra("CLIENT_NAME", item.name)
            putExtra("CLIENT_ADDRESS", item.address)
        }
        startActivity(intent)
    }

    // ── GPS MAP ──

    private fun setupMap() {
        mapView = binding.mapView
        mapView?.setTileSource(TileSourceFactory.MAPNIK)
        mapView?.setMultiTouchControls(true)
        val mapController = mapView?.controller
        mapController?.setZoom(7.0)
        mapController?.setCenter(GeoPoint(51.9194, 19.1451)) // Center of Poland
    }

    private fun loadGpsMarkers() {
        val map = mapView ?: return
        map.overlays.removeAll { it is Marker && it.id != "user_location_marker" }

        val items = organizerManager.getItems()
        var count = 0
        for (item in items) {
            if (item.latitude != 0.0 || item.longitude != 0.0) {
                val marker = Marker(map)
                marker.position = GeoPoint(item.latitude, item.longitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = item.name
                marker.snippet = item.address
                marker.setOnMarkerClickListener { m, _ ->
                    m.showInfoWindow()
                    true
                }
                map.overlays.add(marker)
                count++
            }
        }
        binding.gpsInfoText.text = "Mapa kontrahentów ($count pinów)"

        // Zoom to fit markers if any
        if (count > 0) {
            val firstWithCoords = items.first { it.latitude != 0.0 || it.longitude != 0.0 }
            map.controller.setCenter(GeoPoint(firstWithCoords.latitude, firstWithCoords.longitude))
            map.controller.setZoom(10.0)
        }
        map.invalidate()
    }

    private fun syncGpsContractors() {
        val query = binding.etKontrahentSearchInput.text.toString().trim()
        if (query.length < 3) {
            Toast.makeText(this, "Wpisz co najmniej 3 znaki do wyszukania w zakładce Kontrahenci", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Synchronizacja kontrahentów...", Toast.LENGTH_SHORT).show()
        CrmApiSync.syncAddressBook(
            context = this,
            searchQuery = query,
            onSuccess = { responseJson ->
                runOnUiThread {
                    organizerManager.syncFromAddressBook(responseJson)
                    loadGpsMarkers()
                    loadOrganizer()
                    Toast.makeText(this, "Zaktualizowano mapę kontrahentów", Toast.LENGTH_SHORT).show()
                }
            },
            onError = { errorMessage ->
                runOnUiThread {
                    Toast.makeText(this, "Błąd: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // ── LOCATION TRACKING ──

    private fun startLocationTracking() {
        if (!hasPermissions()) return
        
        // Batter-efficient balanced power accuracy & loose intervals/batching (as per design doc rules 6 & 7)
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60000L).apply {
            setMinUpdateIntervalMillis(30000L)
            setMaxUpdateDelayMillis(300000L) // 5 minutes batching to allow CPU sleep/batch updates
        }.build()
 
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val loc = locationResult.lastLocation ?: return
                
                // Run on UI thread to update map overlays
                runOnUiThread {
                    mapView?.let { map ->
                        // Remove previous user location marker
                        map.overlays.removeAll { it is Marker && it.id == "user_location_marker" }
                        
                        val userMarker = Marker(map)
                        userMarker.id = "user_location_marker"
                        userMarker.position = GeoPoint(loc.latitude, loc.longitude)
                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        userMarker.title = "Twoja lokalizacja"
                        // Custom icon or colors can be set, using default for now
                        map.overlays.add(userMarker)
                        
                        if (activeTab == "gps") {
                            map.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                        }
                    }
                }
                
                checkLocationProximity(loc.latitude, loc.longitude)
            }
        }
 
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                mainLooper
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopLocationTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private fun checkLocationProximity(lat: Double, lon: Double) {
        val items = organizerManager.getItems()
        val now = System.currentTimeMillis()
        val RADIUS_METERS = 1000.0 // 1 km
        val DWELL_TIME_MS = 5 * 60 * 1000L // 5 minutes

        for (item in items) {
            val distance = calculateDistance(lat, lon, item.latitude, item.longitude)
            if (distance < RADIUS_METERS) {
                val firstEntry = dwellTracker[item.id]
                if (firstEntry == null) {
                    // First time entering zone — start timer
                    dwellTracker[item.id] = now
                } else if (now - firstEntry >= DWELL_TIME_MS) {
                    // Been in zone for >= 5 minutes — trigger notification
                    sendPresenceNotification(item)
                    dwellTracker.remove(item.id) // Reset after notification
                }
            } else {
                // Left the zone — clear tracker
                dwellTracker.remove(item.id)
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    // ── NOTIFICATIONS ──

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "CRM Organizer Alerts"
            val descriptionText = "Powiadomienia o pobycie u kontrahenta"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendPresenceNotification(item: OrganizerItem) {
        // Prevent spamming notification if shown recently
        val prefs = getSharedPreferences("notification_spam_guard", Context.MODE_PRIVATE)
        val lastTime = prefs.getLong(item.id, 0L)
        val now = System.currentTimeMillis()
        if (now - lastTime < 600000) { // 10 minutes throttle
            return
        }
        prefs.edit().putLong(item.id, now).apply()

        val intent = Intent(this, VisitPanelActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("CLIENT_ID", item.id)
            putExtra("CLIENT_NAME", item.name)
            putExtra("CLIENT_ADDRESS", item.address)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            item.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_check) // Reusing existing check drawable
            .setContentTitle("Wizyta u kontrahenta!")
            .setContentText("Jesteś u: ${item.name}. Kliknij aby zapisać notatkę z wizyty.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(this)) {
                if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                    notify(item.hashCode(), builder.build())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun simulatePresenceNotification(item: OrganizerItem) {
        Toast.makeText(this, "Symulacja przybycia do: ${item.name}", Toast.LENGTH_SHORT).show()
        // Override notification throttle for manual simulation clicks
        val prefs = getSharedPreferences("notification_spam_guard", Context.MODE_PRIVATE)
        prefs.edit().remove(item.id).apply()
        
        sendPresenceNotification(item)
    }

    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
        startActivity(intent)
    }
}

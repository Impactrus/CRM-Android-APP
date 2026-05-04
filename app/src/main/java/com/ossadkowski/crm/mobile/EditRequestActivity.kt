package com.ossadkowski.crm.mobile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.data.SessionManager
import com.ossadkowski.crm.mobile.data.model.CreateWniosekRequest
import com.ossadkowski.crm.mobile.data.model.SlownikItemDto
import com.ossadkowski.crm.mobile.data.model.WniosekDetailDto
import com.ossadkowski.crm.mobile.ui.editrequest.EditRequestViewModel
import java.io.File

class EditRequestActivity : BaseActivity() {

    private val viewModel: EditRequestViewModel by viewModels()
    private lateinit var session: SessionManager

    private var wniosekId = 0
    private var isEditable = false
    private var loadedDetail: WniosekDetailDto? = null

    private var typyList: List<SlownikItemDto> = emptyList()
    private var rodzajeUrlopuList: List<SlownikItemDto> = emptyList()
    private var uzytkownicyList: List<SlownikItemDto> = emptyList()

    private val photoUris = mutableListOf<Uri>()

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoUris.add(uri)
            refreshPhotosUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_request)

        session = SessionManager(this)
        wniosekId = intent.getIntExtra("id", 0)

        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }

        setupFormObservers()
        setupDetailObserver()
        setupUpdateObserver()

        viewModel.loadFormData()
        viewModel.loadDetail(wniosekId)
    }

    private fun setupDetailObserver() {
        viewModel.detail.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    val w = result.data ?: return@observe
                    loadedDetail = w
                    val status = w.status ?: ""
                    isEditable = status in listOf("Szkic", "Do poprawy", "Do poprawy (HR)")

                    // Set title
                    findViewById<TextView>(R.id.topbar_title)?.text =
                        if (isEditable) getString(R.string.edit_request_title) else getString(R.string.view_request_title)

                    fillForm(w)
                    setFieldsEnabled(isEditable)

                    // Submit button
                    val btnSubmit = findViewById<Button>(R.id.btn_submit)
                    if (isEditable) {
                        btnSubmit.text = getString(R.string.save_changes)
                        btnSubmit.visibility = View.VISIBLE
                        btnSubmit.setOnClickListener { submitUpdate() }
                    } else {
                        btnSubmit.visibility = View.GONE
                    }

                    // Photo button
                    val btnAddPhoto = findViewById<Button>(R.id.btn_add_photo)
                    if (isEditable) {
                        btnAddPhoto.visibility = View.VISIBLE
                        btnAddPhoto.setOnClickListener {
                            galleryLauncher.launch("image/*")
                        }
                    } else {
                        btnAddPhoto.visibility = View.GONE
                    }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun fillForm(w: WniosekDetailDto) {
        // Parse dates from odDo (format: "2026-03-26 - 2026-03-27" or "23.03.2026")
        val dates = w.odDo?.split(" - ", " – ") ?: listOf("", "")
        findViewById<EditText>(R.id.input_start_date).setText(dates.getOrElse(0) { "" })
        findViewById<EditText>(R.id.input_end_date).setText(dates.getOrElse(1) { dates.getOrElse(0) { "" } })
        findViewById<EditText>(R.id.input_description).setText(w.powod ?: "")

        // Select type in spinner (after data loads)
        selectSpinnerByName(findViewById(R.id.spinner_request_type), typyList, w.typ)

        // Select leave type
        if (w.typ == "Urlop") {
            findViewById<View>(R.id.label_leave_type).visibility = View.VISIBLE
            findViewById<Spinner>(R.id.spinner_leave_type).visibility = View.VISIBLE
            selectSpinnerByName(findViewById(R.id.spinner_leave_type), rodzajeUrlopuList, w.rodzajUrlopu)
        }

        // Select substitute
        val subSpinner = findViewById<Spinner>(R.id.spinner_substitute)
        if (w.zastepstwoUserId != null) {
            val idx = uzytkownicyList.indexOfFirst { it.id == w.zastepstwoUserId }
            if (idx >= 0) subSpinner.setSelection(idx + 1) // +1 for "-- Brak --"
        }
    }

    private fun selectSpinnerByName(spinner: Spinner, list: List<SlownikItemDto>, name: String?) {
        if (name == null) return
        val idx = list.indexOfFirst { it.nazwa == name }
        if (idx >= 0) spinner.setSelection(idx)
    }

    private fun setFieldsEnabled(enabled: Boolean) {
        findViewById<Spinner>(R.id.spinner_request_type).isEnabled = enabled
        findViewById<Spinner>(R.id.spinner_leave_type).isEnabled = enabled
        findViewById<Spinner>(R.id.spinner_substitute).isEnabled = enabled

        val startDate = findViewById<EditText>(R.id.input_start_date)
        val endDate = findViewById<EditText>(R.id.input_end_date)
        val description = findViewById<EditText>(R.id.input_description)

        startDate.isEnabled = enabled
        startDate.isFocusable = false
        startDate.isClickable = enabled
        if (enabled) startDate.setOnClickListener { showDatePicker(startDate) }

        endDate.isEnabled = enabled
        endDate.isFocusable = false
        endDate.isClickable = enabled
        if (enabled) endDate.setOnClickListener { showDatePicker(endDate) }

        description.isEnabled = enabled
        description.isFocusableInTouchMode = enabled

        val alpha = if (enabled) 1.0f else 0.6f
        findViewById<Spinner>(R.id.spinner_request_type).alpha = alpha
        findViewById<Spinner>(R.id.spinner_leave_type).alpha = alpha
        findViewById<Spinner>(R.id.spinner_substitute).alpha = alpha
        startDate.alpha = alpha
        endDate.alpha = alpha
        description.alpha = alpha
    }

    private fun setupFormObservers() {
        val spinnerRequest = findViewById<Spinner>(R.id.spinner_request_type)
        val spinnerLeave = findViewById<Spinner>(R.id.spinner_leave_type)
        val spinnerSub = findViewById<Spinner>(R.id.spinner_substitute)
        val labelLeave = findViewById<View>(R.id.label_leave_type)

        viewModel.typy.observe(this) { result ->
            if (result is NetworkResult.Success && result.data != null) {
                typyList = result.data
                val names = typyList.map { it.nazwa }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerRequest.adapter = adapter

                spinnerRequest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                        val selectedTyp = typyList.getOrNull(pos)?.nazwa ?: ""
                        val isUrlop = selectedTyp == "Urlop"
                        labelLeave.visibility = if (isUrlop) View.VISIBLE else View.GONE
                        spinnerLeave.visibility = if (isUrlop) View.VISIBLE else View.GONE
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Re-fill after spinner loaded
                loadedDetail?.let { selectSpinnerByName(spinnerRequest, typyList, it.typ) }
            }
        }

        viewModel.rodzajeUrlopu.observe(this) { result ->
            if (result is NetworkResult.Success && result.data != null) {
                rodzajeUrlopuList = result.data
                val names = rodzajeUrlopuList.map { it.nazwa }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerLeave.adapter = adapter
                loadedDetail?.let { selectSpinnerByName(spinnerLeave, rodzajeUrlopuList, it.rodzajUrlopu) }
            }
        }

        viewModel.uzytkownicy.observe(this) { result ->
            if (result is NetworkResult.Success && result.data != null) {
                uzytkownicyList = result.data
                val names = listOf(getString(R.string.substitute_none)) + uzytkownicyList.map { it.nazwa }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSub.adapter = adapter
                loadedDetail?.let { d ->
                    if (d.zastepstwoUserId != null) {
                        val idx = uzytkownicyList.indexOfFirst { it.id == d.zastepstwoUserId }
                        if (idx >= 0) spinnerSub.setSelection(idx + 1)
                    }
                }
            }
        }
    }

    private fun setupUpdateObserver() {
        viewModel.updateResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> findViewById<Button>(R.id.btn_submit).isEnabled = false
                is NetworkResult.Success -> {
                    Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()
                    finish()
                }
                is NetworkResult.Error -> {
                    findViewById<Button>(R.id.btn_submit).isEnabled = true
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun submitUpdate() {
        val spinnerRequest = findViewById<Spinner>(R.id.spinner_request_type)
        val spinnerLeave = findViewById<Spinner>(R.id.spinner_leave_type)
        val spinnerSub = findViewById<Spinner>(R.id.spinner_substitute)
        val startDate = findViewById<EditText>(R.id.input_start_date).text.toString()
        val endDate = findViewById<EditText>(R.id.input_end_date).text.toString()
        val description = findViewById<EditText>(R.id.input_description).text.toString()

        val typIndex = spinnerRequest.selectedItemPosition
        val typ = typyList.getOrNull(typIndex)?.nazwa ?: ""

        if (typ.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, R.string.validation_enter_description, Toast.LENGTH_SHORT).show()
            return
        }

        val rodzajUrlopu = if (typ == "Urlop") {
            val leaveIndex = spinnerLeave.selectedItemPosition
            rodzajeUrlopuList.getOrNull(leaveIndex)?.nazwa
        } else null

        val subIndex = spinnerSub.selectedItemPosition
        val zastepstwoUserId = if (subIndex > 0) uzytkownicyList.getOrNull(subIndex - 1)?.id else null

        val odDo = "$startDate - $endDate"
        val iloscDni = try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val d1 = sdf.parse(startDate)
            val d2 = sdf.parse(endDate)
            if (d1 != null && d2 != null) {
                val diff = (d2.time - d1.time) / (1000 * 60 * 60 * 24)
                maxOf(diff.toInt() + 1, 1)
            } else 1
        } catch (e: Exception) { 1 }

        val request = CreateWniosekRequest(
            userId = session.userId,
            typ = typ,
            rodzajUrlopu = rodzajUrlopu,
            odDo = odDo,
            powod = description,
            iloscDni = iloscDni,
            zastepstwoUserId = zastepstwoUserId
        )

        viewModel.update(wniosekId, request)
    }


    private fun refreshPhotosUI() {
        val container = findViewById<LinearLayout>(R.id.photos_container)
        container.removeAllViews()
        for ((index, uri) in photoUris.withIndex()) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            val thumbnail = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dpToPx(64), dpToPx(64)).apply { marginEnd = dpToPx(12) }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }
            val nameText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "Zdjęcie ${index + 1}"
                setTextColor(ContextCompat.getColor(this@EditRequestActivity, R.color.crm_heading))
            }
            val removeBtn = TextView(this).apply {
                text = getString(R.string.remove_photo)
                setTextColor(ContextCompat.getColor(this@EditRequestActivity, R.color.status_error_text))
                setOnClickListener {
                    photoUris.removeAt(index)
                    refreshPhotosUI()
                }
            }
            row.addView(thumbnail)
            row.addView(nameText)
            row.addView(removeBtn)
            container.addView(row)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun showDatePicker(input: EditText) {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH)
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        android.app.DatePickerDialog(this, { _, y, m, d ->
            input.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
        }, year, month, day).show()
    }
}

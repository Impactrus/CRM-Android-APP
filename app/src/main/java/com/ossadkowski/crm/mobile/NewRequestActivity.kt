package com.ossadkowski.crm.mobile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
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
import com.ossadkowski.crm.mobile.data.api.RetrofitClient
import com.ossadkowski.crm.mobile.data.model.CreateWniosekRequest
import com.ossadkowski.crm.mobile.data.model.SlownikItemDto
import com.ossadkowski.crm.mobile.ui.newrequest.NewRequestViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewRequestActivity : BaseActivity() {

    private val viewModel: NewRequestViewModel by viewModels()
    private lateinit var session: SessionManager

    private var typyList: List<SlownikItemDto> = emptyList()
    private var rodzajeUrlopuList: List<SlownikItemDto> = emptyList()
    private var uzytkownicyList: List<SlownikItemDto> = emptyList()
    private var homeOfficeAddresses: com.ossadkowski.crm.mobile.data.model.HomeOfficeAddressListDto? = null

    private val photoUris = mutableListOf<Uri>()
    private var zamrozenieWarningShown = false

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

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener { finish() }

        setupObservers()
        setupDatePickers()

        val submitButton = findViewById<Button>(R.id.btn_submit)
        submitButton.setOnClickListener { submitForm() }

        findViewById<Button>(R.id.btn_add_photo).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        val uploadHeader = findViewById<LinearLayout>(R.id.upload_header)
        val uploadContent = findViewById<LinearLayout>(R.id.upload_content)
        val uploadChevron = findViewById<ImageView>(R.id.upload_chevron)

        uploadHeader.setOnClickListener {
            if (uploadContent.visibility == View.VISIBLE) {
                uploadContent.visibility = View.GONE
                uploadChevron.animate().rotation(0f).setDuration(200).start()
            } else {
                uploadContent.visibility = View.VISIBLE
                uploadChevron.animate().rotation(180f).setDuration(200).start()
            }
        }

        viewModel.loadFormData()
    }

    private fun onDatesChanged() {
        val startStr = findViewById<EditText>(R.id.input_start_date).text.toString()
        val endStr = findViewById<EditText>(R.id.input_end_date).text.toString()
        val infoView = findViewById<TextView>(R.id.working_days_info)

        if (startStr.isEmpty() || endStr.isEmpty()) {
            infoView.visibility = View.GONE
            return
        }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val startDate = sdf.parse(startStr) ?: return
            val endDate = sdf.parse(endStr) ?: return

            if (endDate.before(startDate)) {
                infoView.visibility = View.GONE
                return
            }

            // Count working days (Mon-Fri)
            val cal = Calendar.getInstance()
            cal.time = startDate
            var workingDays = 0
            while (!cal.time.after(endDate)) {
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) {
                    workingDays++
                }
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }

            infoView.text = "$startStr  —  $endStr  ($workingDays dni roboczych)"
            infoView.visibility = View.VISIBLE

            // Check zamrożenia
            checkZamrozenie(startStr, endStr)

        } catch (_: Exception) {
            infoView.visibility = View.GONE
        }
    }

    private fun checkZamrozenie(od: String, do_: String) {
        zamrozenieWarningShown = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = RetrofitClient.apiService.checkZamrozenie(session.userId, od, do_)
                if (result.zamrozone) {
                    withContext(Dispatchers.Main) {
                        zamrozenieWarningShown = true
                        android.app.AlertDialog.Builder(this@NewRequestActivity)
                            .setTitle("Zamrożenie urlopów")
                            .setMessage("W wybranym okresie obowiązuje zamrożenie urlopów" +
                                (if (result.dzial != null) " dla działu ${result.dzial}" else "") +
                                " (${result.dataOd} - ${result.dataDo})" +
                                (if (!result.opis.isNullOrBlank()) ".\n\nPowód: ${result.opis}" else "") +
                                ".\n\nWniosek zostanie oznaczony jako złożony w okresie zamrożenia.")
                            .setPositiveButton("Rozumiem", null)
                            .show()
                    }
                }
            } catch (_: Exception) { }
        }
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
                layoutParams = LinearLayout.LayoutParams(64.dpToPx(), 64.dpToPx()).apply {
                    marginEnd = 12.dpToPx()
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(uri)
            }

            val nameText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "Zdjęcie ${index + 1}"
                setTextColor(ContextCompat.getColor(this@NewRequestActivity, R.color.crm_heading))
            }

            val removeBtn = TextView(this).apply {
                text = getString(R.string.remove_photo)
                setTextColor(ContextCompat.getColor(this@NewRequestActivity, R.color.status_error_text))
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

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun setupObservers() {
        val spinnerRequest = findViewById<Spinner>(R.id.spinner_request_type)
        val spinnerLeave = findViewById<Spinner>(R.id.spinner_leave_type)
        val spinnerSub = findViewById<Spinner>(R.id.spinner_substitute)
        val labelLeave = findViewById<View>(R.id.label_leave_type)
        val labelSub = findViewById<View>(R.id.label_substitute)

        // Ukryj sekcję rodzaju urlopu i zastępstwa domyślnie
        labelLeave.visibility = View.GONE
        spinnerLeave.visibility = View.GONE
        labelSub.visibility = View.GONE
        spinnerSub.visibility = View.GONE

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
                        val isDelegacja = selectedTyp == "Delegacja" || selectedTyp == "Delegacje"
                        val isRemote = selectedTyp == "Praca zdalna"

                        // Rodzaj urlopu – tylko dla Urlopu
                        labelLeave.visibility = if (isUrlop) View.VISIBLE else View.GONE
                        spinnerLeave.visibility = if (isUrlop) View.VISIBLE else View.GONE

                        // Zastępstwo – dla Urlopu i Delegacji
                        val showSub = isUrlop || isDelegacja
                        labelSub.visibility = if (showSub) View.VISIBLE else View.GONE
                        spinnerSub.visibility = if (showSub) View.VISIBLE else View.GONE

                        // Praca Zdalna section
                        val sectionRemote = findViewById<View>(R.id.section_remote_work)
                        sectionRemote.visibility = if (isRemote) View.VISIBLE else View.GONE
                        if (isRemote) {
                            viewModel.loadHomeOfficeData(session.userId)
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        viewModel.rodzajeUrlopu.observe(this) { result ->
            if (result is NetworkResult.Success && result.data != null) {
                rodzajeUrlopuList = result.data
                val names = rodzajeUrlopuList.map { it.nazwa }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerLeave.adapter = adapter
            }
        }

        viewModel.uzytkownicy.observe(this) { result ->
            if (result is NetworkResult.Success && result.data != null) {
                uzytkownicyList = result.data
                val names = listOf(getString(R.string.substitute_none)) + uzytkownicyList.map { it.nazwa }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSub.adapter = adapter
            }
        }

        val remainingDaysText = findViewById<TextView>(R.id.remote_work_remaining_days)
        viewModel.homeOfficeSaldo.observe(this) { result ->
            if (result is NetworkResult.Success && result.data != null) {
                remainingDaysText.text = "${result.data.saldo?.toInt() ?: 0} dni"
            }
        }

        val spinnerAddress = findViewById<Spinner>(R.id.spinner_remote_address)
        val inputStreet = findViewById<EditText>(R.id.input_address_street)
        val inputNumber = findViewById<EditText>(R.id.input_address_number)
        val inputCity = findViewById<EditText>(R.id.input_address_city)
        val inputZip = findViewById<EditText>(R.id.input_address_zip)

        viewModel.addresses.observe(this) { result ->
            if (result is NetworkResult.Success && result.data != null) {
                homeOfficeAddresses = result.data
                val options = mutableListOf<String>()
                val addrList = mutableListOf<com.ossadkowski.crm.mobile.data.model.HomeOfficeAddressDto?>()

                val firebird = result.data.firebird
                if (firebird != null) {
                    options.add("Z kadr (Firebird) — ${firebird.miasto ?: ""}")
                    addrList.add(firebird)
                }

                result.data.custom.forEach { custom ->
                    options.add("${custom.label ?: "Adres"} — ${custom.miasto ?: ""}")
                    addrList.add(custom)
                }

                options.add("Inny adres")
                addrList.add(null)

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerAddress.adapter = adapter

                spinnerAddress.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                        val selectedAddr = addrList.getOrNull(pos)
                        if (selectedAddr != null) {
                            inputStreet.setText(selectedAddr.ulica ?: "")
                            inputNumber.setText(selectedAddr.numer ?: "")
                            inputCity.setText(selectedAddr.miasto ?: "")
                            inputZip.setText(selectedAddr.kod ?: "")

                            inputStreet.isEnabled = false
                            inputNumber.isEnabled = false
                            inputCity.isEnabled = false
                            inputZip.isEnabled = false
                        } else {
                            inputStreet.setText("")
                            inputNumber.setText("")
                            inputCity.setText("")
                            inputZip.setText("")

                            inputStreet.isEnabled = true
                            inputNumber.isEnabled = true
                            inputCity.isEnabled = true
                            inputZip.isEnabled = true
                        }
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        viewModel.submitResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    findViewById<Button>(R.id.btn_submit).isEnabled = false
                }
                is NetworkResult.Success -> {
                    Toast.makeText(this, R.string.wniosek_created, Toast.LENGTH_LONG).show()
                    finish()
                }
                is NetworkResult.Error -> {
                    findViewById<Button>(R.id.btn_submit).isEnabled = true
                    Toast.makeText(this, getString(R.string.error_generic, result.message), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun submitForm() {
        val spinnerRequest = findViewById<Spinner>(R.id.spinner_request_type)
        val spinnerLeave = findViewById<Spinner>(R.id.spinner_leave_type)
        val spinnerSub = findViewById<Spinner>(R.id.spinner_substitute)
        val startDate = findViewById<EditText>(R.id.input_start_date).text.toString()
        val endDate = findViewById<EditText>(R.id.input_end_date).text.toString()
        val description = findViewById<EditText>(R.id.input_description).text.toString()

        val typIndex = spinnerRequest.selectedItemPosition
        val typ = typyList.getOrNull(typIndex)?.nazwa ?: ""

        if (typ.isEmpty()) {
            Toast.makeText(this, R.string.validation_select_type, Toast.LENGTH_SHORT).show()
            return
        }
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(this, R.string.validation_enter_dates, Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            Toast.makeText(this, R.string.validation_enter_description, Toast.LENGTH_SHORT).show()
            return
        }

        val isRemote = typ == "Praca zdalna"
        var address: com.ossadkowski.crm.mobile.data.model.HomeOfficeAddressDto? = null
        var finalDescription = description

        if (isRemote) {
            val checkboxBhp = findViewById<CheckBox>(R.id.checkbox_bhp)
            val checkboxRodo = findViewById<CheckBox>(R.id.checkbox_rodo)
            if (!checkboxBhp.isChecked || !checkboxRodo.isChecked) {
                Toast.makeText(this, "Musisz zaakceptować oświadczenia BHP i RODO", Toast.LENGTH_SHORT).show()
                return
            }

            val street = findViewById<EditText>(R.id.input_address_street).text.toString().trim()
            val number = findViewById<EditText>(R.id.input_address_number).text.toString().trim()
            val city = findViewById<EditText>(R.id.input_address_city).text.toString().trim()
            val zip = findViewById<EditText>(R.id.input_address_zip).text.toString().trim()

            if (street.isEmpty() || number.isEmpty() || city.isEmpty() || zip.isEmpty()) {
                Toast.makeText(this, "Wypełnij wszystkie pola adresu", Toast.LENGTH_SHORT).show()
                return
            }

            address = com.ossadkowski.crm.mobile.data.model.HomeOfficeAddressDto(
                ulica = street,
                numer = number,
                miasto = city,
                kod = zip
            )
            finalDescription = "[Adres: $street $number, $zip $city] $description".trim()
        }

        val rodzajUrlopu = if (typ == "Urlop") {
            val leaveIndex = spinnerLeave.selectedItemPosition
            rodzajeUrlopuList.getOrNull(leaveIndex)?.nazwa
        } else null

        val subIndex = spinnerSub.selectedItemPosition
        val zastepstwoUserId = if (subIndex > 0) uzytkownicyList.getOrNull(subIndex - 1)?.id else null

        val odDo = "$startDate - $endDate"

        // Calculate working days
        val iloscDni = calculateWorkingDays(startDate, endDate)

        val request = CreateWniosekRequest(
            userId = session.userId,
            typ = typ,
            rodzajUrlopu = rodzajUrlopu,
            odDo = odDo,
            powod = finalDescription,
            iloscDni = iloscDni,
            zastepstwoUserId = zastepstwoUserId,
            checkBhp = if (isRemote) true else null,
            checkRodo = if (isRemote) true else null
        )

        viewModel.submitWniosek(request, photoUris.toList(), this, address)
    }

    private fun calculateWorkingDays(startStr: String, endStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val d1 = sdf.parse(startStr) ?: return 1
            val d2 = sdf.parse(endStr) ?: return 1
            val cal = Calendar.getInstance()
            cal.time = d1
            var count = 0
            while (!cal.time.after(d2)) {
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                if (dow != Calendar.SATURDAY && dow != Calendar.SUNDAY) count++
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            maxOf(count, 1)
        } catch (_: Exception) { 1 }
    }

    private fun setupDatePickers() {
        val startDateInput = findViewById<EditText>(R.id.input_start_date)
        startDateInput.setOnClickListener { showDatePicker(startDateInput) { onDatesChanged() } }

        val endDateInput = findViewById<EditText>(R.id.input_end_date)
        endDateInput.setOnClickListener { showDatePicker(endDateInput) { onDatesChanged() } }
    }

    private fun showDatePicker(input: EditText, onSet: (() -> Unit)? = null) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = android.app.DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                input.setText(date)
                onSet?.invoke()
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}

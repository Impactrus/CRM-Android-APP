package com.ossadkowski.crm.mobile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.ossadkowski.crm.mobile.data.NetworkResult
import com.ossadkowski.crm.mobile.databinding.ActivityHrKalendarzBinding
import com.ossadkowski.crm.mobile.ui.calendar.CalendarGridAdapter
import java.text.SimpleDateFormat
import java.util.Locale

class HrKalendarzZamrozenActivity : BaseActivity() {

    private lateinit var binding: ActivityHrKalendarzBinding
    private val viewModel: HrKalendarzViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarGridAdapter
    private val listAdapter = HrZamrozeniaAdapter()

    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("pl", "PL"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHrKalendarzBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        // Instalacja siatki kalendarza
        calendarAdapter = CalendarGridAdapter { day ->
            // kliknij -> w Kalendarzu Zamrożeń kliknięcia nie mają szczególnego wpływu, ew toast
            if (day.hasTasks) {
                Toast.makeText(this, "Ten dzień jest objęty okresem zamrożenia na wnioski URL.", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.recyclerView.layoutManager = GridLayoutManager(this, 7)
        binding.recyclerView.adapter = calendarAdapter
        binding.recyclerView.setHasFixedSize(true)

        // Lista zamrożeń (opisy pod spodem)
        binding.zamrozeniaLista.layoutManager = LinearLayoutManager(this)
        binding.zamrozeniaLista.adapter = listAdapter

        // Obsługa zmian miesięcy
        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }

        // Obserwowanie macierzy kalendarza
        viewModel.daysOfMonth.observe(this) { days ->
            calendarAdapter.submitList(days)
            binding.monthYearText.text = monthFormat.format(viewModel.getCurrentMonthDate()).uppercase()
        }

        // Obserwowanie danych tekstowych
        viewModel.zamrozenia.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> binding.progressBar.visibility = View.VISIBLE
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val data = result.data ?: emptyList()
                    listAdapter.submitList(data)
                    binding.zamrozeniaLabel.text = if (data.isEmpty()) "BRAK ZAMROŻEŃ W TYM MIESIĄCU" else "OPISY ZAMROŻEŃ W TYM MIESIĄCU:"
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

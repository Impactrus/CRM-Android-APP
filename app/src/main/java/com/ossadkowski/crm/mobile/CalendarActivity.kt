package com.ossadkowski.crm.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.ossadkowski.crm.mobile.databinding.ActivityCalendarBinding
import com.ossadkowski.crm.mobile.ui.calendar.CalendarGridAdapter
import com.ossadkowski.crm.mobile.ui.calendar.CalendarTasksAdapter
import com.ossadkowski.crm.mobile.ui.calendar.CalendarViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : BaseActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private val viewModel: CalendarViewModel by viewModels()
    private lateinit var calendarAdapter: CalendarGridAdapter
    private lateinit var tasksAdapter: CalendarTasksAdapter

    private val monthNames = arrayOf("", "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec", "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()

        viewModel.loadMonth()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener { finish() }

        calendarAdapter = CalendarGridAdapter { day ->
            if (day.isCurrentMonth) {
                viewModel.selectDay(day.date)
                // Usunięcie bezpośredniego updateSelectedDayLabel stąd, 
                // bo observer wyżej zajmie się widocznością/tekstem.
            }
        }

        tasksAdapter = CalendarTasksAdapter { task ->
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra("TASK_ID", task.id)
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@CalendarActivity, 7)
            adapter = calendarAdapter
            setHasFixedSize(true)
        }

        binding.tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CalendarActivity)
            adapter = tasksAdapter
        }

        binding.btnPrevMonth.setOnClickListener { viewModel.prevMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }
    }

    private fun observeViewModel() {
        viewModel.days.observe(this) { daysList ->
            calendarAdapter.submitList(daysList)
            updateLabel()
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.selectedDayTasks.observe(this) { tasks ->
            tasksAdapter.submitList(tasks)
            // Aktualizujemy tytuł z datą wybranego dnia
            val date = viewModel.selectedDate
            if (date != null) {
                val localePl = Locale("pl", "PL")
                val format = SimpleDateFormat("EEEE, d MMMM", localePl)
                if (tasks.isEmpty()) {
                    binding.selectedDayLabel.text = format.format(date)
                    binding.tasksRecyclerView.visibility = View.GONE
                    binding.emptyTasksView.visibility = View.VISIBLE
                } else {
                    binding.selectedDayLabel.text = "${format.format(date)} (${tasks.size})"
                    binding.tasksRecyclerView.visibility = View.VISIBLE
                    binding.emptyTasksView.visibility = View.GONE
                }
            }
        }

        viewModel.error.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateSelectedDayLabel(date: Date) {
        val localePl = Locale("pl", "PL")
        val format = SimpleDateFormat("EEEE, d MMMM", localePl)
        binding.selectedDayLabel.text = "Zadania na: ${format.format(date)}"
    }

    private fun updateLabel() {
        binding.monthLabel.text = "${monthNames[viewModel.currentMonth]} ${viewModel.currentYear}"
    }
}

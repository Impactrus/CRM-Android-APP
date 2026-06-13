package com.ossadkowski.crm.mobile.ui.vacation

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ossadkowski.crm.mobile.data.model.VacationFreezeDto
import com.ossadkowski.crm.mobile.data.model.TeamEmployeePlanDto
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun VacationScreen(viewModel: VacationViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = true) {
        viewModel.toastEvent.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        bottomBar = {
            if (selectedTab == 0) {
                MyPlanBottomBar(viewModel)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            if (viewModel.isManager) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF4D6B13)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Mój plan", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Plany zespołu", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    )
                }
            }

            if (selectedTab == 0) {
                MyPlanTab(viewModel)
            } else {
                TeamPlansTab(viewModel)
            }
        }
    }
}

@Composable
fun MyPlanTab(viewModel: VacationViewModel) {
    val myPlan = viewModel.myPlan
    val submission = viewModel.submissionInfo

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Year Switcher & Status Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.changeYear(-1) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Poprzedni rok", tint = Color(0xFF4D6B13))
                    }
                    Text(
                        text = viewModel.currentYear.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    IconButton(onClick = { viewModel.changeYear(1) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Następny rok", tint = Color(0xFF4D6B13))
                    }
                }

                val status = viewModel.currentStatus
                val statusLabel = when (status) {
                    "draft" -> "Szkic"
                    "submitted" -> "Do akceptacji"
                    "approved" -> "Zatwierdzony"
                    "rejected" -> "Odrzucony"
                    "revoked" -> "Wycofany"
                    else -> status
                }
                val statusBg = when (status) {
                    "draft", "revoked" -> Color(0xFFE5E7EB)
                    "submitted" -> Color(0xFFFEF3C7)
                    "approved" -> Color(0xFFD1FAE5)
                    "rejected" -> Color(0xFFFEE2E2)
                    else -> Color(0xFFE5E7EB)
                }
                val statusText = when (status) {
                    "draft", "revoked" -> Color(0xFF374151)
                    "submitted" -> Color(0xFFD97706)
                    "approved" -> Color(0xFF065F46)
                    "rejected" -> Color(0xFF991B1B)
                    else -> Color(0xFF374151)
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Rejection comment
        if (viewModel.currentStatus == "rejected" && viewModel.rejectReason.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    border = BorderStroke(1.dp, Color(0xFFF87171)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Powód odrzucenia:", fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        Text(viewModel.rejectReason, color = Color(0xFF7F1D1D))
                    }
                }
            }
        }

        // Balance Cards Grid
        item {
            myPlan?.balance?.let { balance ->
                val limit = balance.vacDays
                val prev = balance.prevLimitD
                val totalLimit = limit + prev
                val rest = balance.restLimitD
                val planned = viewModel.localPlannedDates.size

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BalanceCard("Limit na rok", "$totalLimit dni", Color(0xFFD1FAE5), Color(0xFF065F46), Modifier.weight(1f))
                        BalanceCard("Pozostało", "$rest dni", Color(0xFFDBEAFE), Color(0xFF1E40AF), Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BalanceCard("Wykorzystane", "${balance.limitConsD} dni", Color(0xFFF3F4F6), Color(0xFF374151), Modifier.weight(1f))
                        BalanceCard("Zaplanowane", "$planned dni", Color(0xFFFEF3C7), Color(0xFF92400E), Modifier.weight(1f))
                    }
                }
            }
        }



        // Calendar Grid
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Kalendarz planowania",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    CalendarGridMatrix(viewModel)
                }
            }
        }

        // Expandable Full Calendar Matrix Preview (under the monthly calendar card)
        item {
            var expandedFull by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedFull = !expandedFull },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Podgląd całego roku (szkic planu)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = if (expandedFull) "Zwiń ▲" else "Rozwiń ▼",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4D6B13)
                        )
                    }
                    if (expandedFull) {
                        Spacer(modifier = Modifier.height(12.dp))
                        CalendarGridMatrixFull(viewModel)
                    }
                }
            }
        }

        // Expandable Freezes Alert (under calendar)
        if (viewModel.globalFreezes.isNotEmpty()) {
            item {
                var expanded by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️ ", fontSize = 20.sp)
                                Text(
                                    text = "Zamrożenia kalendarza (${viewModel.globalFreezes.size})",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = if (expanded) "Zwiń ▲" else "Rozwiń ▼",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                viewModel.globalFreezes.forEach { freeze ->
                                    Text(
                                        text = "• ${freeze.opis ?: freeze.dzial ?: "Zamrożenie"} (${freeze.dataOd} – ${freeze.dataDo})",
                                        fontSize = 13.sp,
                                        color = Color(0xFF78350F)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Legend
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Legenda", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827))
                    LegendItem("Dzień roboczy", Color.White, border = Color(0xFFE5E7EB))
                    LegendItem("Weekend", Color(0xFFF3F4F6))
                    LegendItem("Święto", Color(0xFFE5E7EB))
                    LegendItem("Zamrożenie działu", Color(0xFFFEE2E2), border = Color(0xFFEF4444))
                    LegendItem("Zaplanowany urlop (szkic)", Color(0xFF97C11E))
                    LegendItem("Wniosek wysłany", Color(0xFF4D6B13))
                    LegendItem("Konflikt z zamrożeniem", Color(0xFFDC2626))
                }
            }
        }
    }
}

@Composable
fun BalanceCard(title: String, value: String, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 13.sp, color = textColor.copy(alpha = 0.8f))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, border: Color? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
                .let {
                    if (border != null) it.border(1.dp, border, shape = RoundedCornerShape(2.dp)) else it
                }
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 13.sp, color = Color(0xFF374151))
    }
}

data class ComposeCalendarDay(
    val dateStr: String,
    val dayNum: Int,
    val isCurrentMonth: Boolean,
    val isWeekend: Boolean,
    val isHoliday: Boolean
)

@Composable
fun CalendarGridMatrix(viewModel: VacationViewModel) {
    val year = viewModel.currentYear
    val myPlan = viewModel.myPlan ?: return
    val holidays = viewModel.getHolidaysForYear(year)

    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }

    val monthNamesPl = listOf(
        "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
        "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // Month Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (selectedMonth == 1) {
                    selectedMonth = 12
                    viewModel.changeYear(-1)
                } else {
                    selectedMonth -= 1
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Poprzedni miesiąc",
                    tint = Color(0xFF4D6B13)
                )
            }

            Text(
                text = "${monthNamesPl[selectedMonth - 1].uppercase()} $year",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111827)
            )

            IconButton(onClick = {
                if (selectedMonth == 12) {
                    selectedMonth = 1
                    viewModel.changeYear(1)
                } else {
                    selectedMonth += 1
                }
            }) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Następny miesiąc",
                    tint = Color(0xFF4D6B13)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Weekdays Header Row
        val weekdays = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { dayName ->
                Box(
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        // Generate 42 cells grid
        val daysList = remember(year, selectedMonth) {
            val list = mutableListOf<ComposeCalendarDay>()
            val cal = java.util.GregorianCalendar(year, selectedMonth - 1, 1)
            val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

            var startDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            if (startDayOfWeek == 0) startDayOfWeek = 7

            // Previous month filler days
            val prevCal = cal.clone() as Calendar
            prevCal.add(Calendar.MONTH, -1)
            val prevMaxDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val prevYear = prevCal.get(Calendar.YEAR)
            val prevMonth = prevCal.get(Calendar.MONTH) + 1

            for (i in 1 until startDayOfWeek) {
                val dayNum = prevMaxDays - startDayOfWeek + i + 1
                val dateStr = String.format("%d-%02d-%02d", prevYear, prevMonth, dayNum)
                val isWeekendVal = isWeekend(prevYear, prevMonth, dayNum)
                val isHolidayVal = holidays.contains(dateStr)
                list.add(ComposeCalendarDay(dateStr, dayNum, false, isWeekendVal, isHolidayVal))
            }

            // Current month days
            for (day in 1..maxDays) {
                val dateStr = String.format("%d-%02d-%02d", year, selectedMonth, day)
                val isWeekendVal = isWeekend(year, selectedMonth, day)
                val isHolidayVal = holidays.contains(dateStr)
                list.add(ComposeCalendarDay(dateStr, day, true, isWeekendVal, isHolidayVal))
            }

            // Next month filler days to complete 42 cells
            val nextCal = cal.clone() as Calendar
            nextCal.add(Calendar.MONTH, 1)
            val nextYear = nextCal.get(Calendar.YEAR)
            val nextMonth = nextCal.get(Calendar.MONTH) + 1
            var nextDayNum = 1
            while (list.size < 42) {
                val dateStr = String.format("%d-%02d-%02d", nextYear, nextMonth, nextDayNum)
                val isWeekendVal = isWeekend(nextYear, nextMonth, nextDayNum)
                val isHolidayVal = holidays.contains(dateStr)
                list.add(ComposeCalendarDay(dateStr, nextDayNum, false, isWeekendVal, isHolidayVal))
                nextDayNum++
            }
            list
        }

        // Draw 6 rows x 7 days grid
        for (row in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (col in 0 until 7) {
                    val index = row * 7 + col
                    val dayData = daysList[index]
                    val dateStr = dayData.dateStr
                    val isCurrentMonth = dayData.isCurrentMonth
                    val isWeekend = dayData.isWeekend
                    val isHoliday = dayData.isHoliday

                    val freeze = getFreeze(dateStr, viewModel.globalFreezes)
                    val isPlanned = viewModel.localPlannedDates.contains(dateStr)
                    val isSubmitted = myPlan.plannedDates.contains(dateStr) && viewModel.currentStatus != "draft" && viewModel.currentStatus != "revoked"
                    val isConflict = (isPlanned || isSubmitted) && freeze != null

                    val cellColor = when {
                        !isCurrentMonth -> Color(0xFFF9FAFB)
                        isConflict -> Color(0xFFDC2626) // Konflikt
                        isSubmitted -> Color(0xFF4D6B13) // Wysłany
                        isPlanned -> Color(0xFF97C11E) // Zaplanowany
                        freeze != null -> Color(0xFFFEE2E2) // Zamrożenie
                        isHoliday -> Color(0xFFE5E7EB) // Święto
                        isWeekend -> Color(0xFFF3F4F6) // Weekend
                        else -> Color.White // Roboczy
                    }

                    val borderStroke = when {
                        !isCurrentMonth -> BorderStroke(0.5.dp, Color(0xFFE5E7EB))
                        freeze != null && !isConflict -> BorderStroke(1.dp, Color(0xFFEF4444))
                        else -> BorderStroke(0.5.dp, Color(0xFFE5E7EB))
                    }

                    val textColor = when {
                        !isCurrentMonth -> Color(0xFFD1D5DB)
                        isConflict || isSubmitted -> Color.White
                        freeze != null -> Color(0xFFB91C1C)
                        isWeekend || isHoliday -> Color(0xFF9CA3AF)
                        else -> Color(0xFF111827)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(cellColor)
                            .border(borderStroke)
                            .clickable(enabled = isCurrentMonth) {
                                viewModel.toggleDay(dateStr)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayData.dayNum.toString(),
                            fontSize = 12.sp,
                            color = textColor,
                            fontWeight = if ((isPlanned || isSubmitted) && isCurrentMonth) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyPlanBottomBar(viewModel: VacationViewModel) {
    val status = viewModel.currentStatus
    val original = viewModel.myPlan?.plannedDates?.toSet() ?: emptySet()
    val isChanged = viewModel.localPlannedDates != original

    if (status == "draft" || status == "rejected" || status == "revoked" || isChanged) {
        Surface(
            tonalElevation = 8.dp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isChanged) {
                    Button(
                        onClick = { viewModel.saveDraft() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4D6B13)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zapisz szkic", color = Color.White)
                    }
                } else if (viewModel.localPlannedDates.isNotEmpty()) {
                    Button(
                        onClick = { viewModel.submitPlan() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF97C11E)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Wyślij do akceptacji", color = Color.White)
                    }
                }

                if (status == "submitted") {
                    Button(
                        onClick = { viewModel.revokePlan() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Wycofaj plan", color = Color.White)
                    }
                }

                if (viewModel.localPlannedDates.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { viewModel.clearPlan() },
                        border = BorderStroke(1.dp, Color(0xFFDC2626)),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("Wyczyść", color = Color(0xFFDC2626))
                    }
                }
            }
        }
    }
}

@Composable
fun TeamPlansTab(viewModel: VacationViewModel) {
    var filterPendingOnly by remember { mutableStateOf(true) }
    var selectedEmployeePlan by remember { mutableStateOf<TeamEmployeePlanDto?>(null) }
    var showDecisionDialog by remember { mutableStateOf(false) }
    var isApproveAction by remember { mutableStateOf(true) }
    var rejectReason by remember { mutableStateOf("") }

    val plansList = if (filterPendingOnly) viewModel.pendingPlans else viewModel.teamPlans

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Filter Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pokazuj tylko oczekujące", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Switch(
                checked = filterPendingOnly,
                onCheckedChange = { filterPendingOnly = it },
                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4D6B13))
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (plansList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Brak planów urlopowych do wyświetlenia.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(plansList) { item ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${item.fname} ${item.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    item.depart?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = {
                                            selectedEmployeePlan = item
                                            isApproveAction = true
                                            showDecisionDialog = true
                                        },
                                        modifier = Modifier.background(Color(0xFFD1FAE5), shape = RoundedCornerShape(4.dp))
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = "Zatwierdź", tint = Color(0xFF065F46))
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedEmployeePlan = item
                                            isApproveAction = false
                                            rejectReason = ""
                                            showDecisionDialog = true
                                        },
                                        modifier = Modifier.background(Color(0xFFFEE2E2), shape = RoundedCornerShape(4.dp))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Odrzuć", tint = Color(0xFF991B1B))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            val submittedDays = item.days.filter { it.status == "submitted" }.map { it.planDate }
                            Text("Zaplanowane dni (${submittedDays.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = if (submittedDays.isEmpty()) "brak" else submittedDays.sorted().joinToString(", "),
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }

    // Decision Dialog
    if (showDecisionDialog && selectedEmployeePlan != null) {
        val employee = selectedEmployeePlan!!
        Dialog(onDismissRequest = { showDecisionDialog = false }) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isApproveAction) "Akceptacja planu" else "Odrzucenie planu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text("Pracownik: ${employee.fname} ${employee.name}")

                    if (!isApproveAction) {
                        OutlinedTextField(
                            value = rejectReason,
                            onValueChange = { rejectReason = it },
                            label = { Text("Powód odrzucenia (min. 10 znaków)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showDecisionDialog = false }) {
                            Text("Anuluj", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val subId = employee.userId // ID pracownika
                                viewModel.decidePlan(
                                    submissionId = subId,
                                    approve = isApproveAction,
                                    rejectReason = if (isApproveAction) null else rejectReason
                                )
                                showDecisionDialog = false
                            },
                            enabled = isApproveAction || rejectReason.trim().length >= 10,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isApproveAction) Color(0xFF065F46) else Color(0xFF991B1B)
                            )
                        ) {
                            Text("Potwierdź", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// Helpers
private fun getDaysInMonth(year: Int, month: Int): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month - 1)
    return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
}

private fun isWeekend(year: Int, month: Int, day: Int): Boolean {
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, day)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
}

private fun getFreeze(dateStr: String, freezes: List<VacationFreezeDto>): VacationFreezeDto? {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    try {
        val current = sdf.parse(dateStr) ?: return null
        return freezes.find {
            val cleanOd = if (it.dataOd.length >= 10) it.dataOd.substring(0, 10) else it.dataOd
            val cleanDo = if (it.dataDo.length >= 10) it.dataDo.substring(0, 10) else it.dataDo
            val from = sdf.parse(cleanOd)
            val to = sdf.parse(cleanDo)
            if (from != null && to != null) {
                !current.before(from) && !current.after(to)
            } else false
        }
    } catch (e: Exception) {
        return null
    }
}

@Composable
fun CalendarGridMatrixFull(viewModel: VacationViewModel) {
    val year = viewModel.currentYear
    val myPlan = viewModel.myPlan ?: return
    val holidays = viewModel.getHolidaysForYear(year)
    val horizontalScrollState = rememberScrollState()

    val monthNames = listOf("Sty", "Lut", "Mar", "Kwi", "Maj", "Cze", "Lip", "Sie", "Wrz", "Paź", "Lis", "Gru")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontalScrollState)
    ) {
        Column {
            // Table Header (Days 1 to 31)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(45.dp)) // Corner cell
                for (day in 1..31) {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Month Rows
            monthNames.forEachIndexed { monthIdx, monthName ->
                val month = monthIdx + 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Month Label
                    Box(
                        modifier = Modifier
                            .width(45.dp)
                            .height(24.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = monthName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                    }

                    // Days Cells
                    val maxDays = getDaysInMonth(year, month)
                    for (day in 1..31) {
                        if (day > maxDays) {
                            // Empty space
                            Box(modifier = Modifier.size(24.dp))
                        } else {
                            val dateStr = String.format("%d-%02d-%02d", year, month, day)
                            val isWeekend = isWeekend(year, month, day)
                            val isHoliday = holidays.contains(dateStr)
                            val freeze = getFreeze(dateStr, viewModel.globalFreezes)

                            val isPlanned = viewModel.localPlannedDates.contains(dateStr)
                            val isSubmitted = myPlan.plannedDates.contains(dateStr) && viewModel.currentStatus != "draft" && viewModel.currentStatus != "revoked"

                            val isConflict = (isPlanned || isSubmitted) && freeze != null

                            val cellColor = when {
                                 isConflict -> Color(0xFFDC2626) // Konflikt
                                 isSubmitted -> Color(0xFF4D6B13) // Wysłany
                                 isPlanned -> Color(0xFF97C11E) // Zaplanowany
                                 freeze != null -> Color(0xFFFEE2E2) // Zamrożenie (jasny czerwony)
                                 isHoliday -> Color(0xFFE5E7EB) // Święto
                                 isWeekend -> Color(0xFFF3F4F6) // Weekend
                                 else -> Color.White // Roboczy
                             }

                             val borderStroke = when {
                                 freeze != null && !isConflict -> BorderStroke(1.dp, Color(0xFFEF4444))
                                 else -> BorderStroke(0.5.dp, Color(0xFFE5E7EB))
                             }

                             val textColor = when {
                                 isConflict || isSubmitted -> Color.White
                                 freeze != null -> Color(0xFFB91C1C) // ciemniejszy czerwony tekst dla kontrastu
                                 isWeekend || isHoliday -> Color(0xFF9CA3AF)
                                 else -> Color(0xFF111827)
                             }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(cellColor)
                                    .border(borderStroke)
                                    .clickable { viewModel.toggleDay(dateStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    fontSize = 10.sp,
                                    color = textColor,
                                    fontWeight = if (isPlanned || isSubmitted) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


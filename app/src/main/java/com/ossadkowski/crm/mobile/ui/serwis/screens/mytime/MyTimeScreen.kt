package com.ossadkowski.crm.mobile.ui.serwis.screens.mytime

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummary
import com.ossadkowski.crm.mobile.domain.serwis.model.TimeSummaryDay
import com.ossadkowski.crm.mobile.ui.serwis.components.BottomCta
import com.ossadkowski.crm.mobile.ui.serwis.components.HeroHeader
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.components.RowCard
import com.ossadkowski.crm.mobile.ui.serwis.components.SectionCard
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.ErrorState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.LoadingState
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTimeScreen(
    vm: MyTimeViewModel = hiltViewModel(),
    onMenuClick: () -> Unit,
    onManualEntryClick: () -> Unit,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    PhoneFrame(
        bottomBar = {
            BottomCta(
                label = "Manualny wpis czasu",
                icon = Icons.Outlined.Add,
                onClick = {
                    // The wiring agent will eventually open a sheet to pick
                    // order + jobCard then route to WorkCard.
                    scope.launch {
                        snackbar.showSnackbar("Wybierz zlecenie z listy Mój dzień")
                    }
                    onManualEntryClick()
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PullToRefreshBox(
                isRefreshing = state is MyTimeUiState.Loading,
                onRefresh = { vm.refresh() },
                state = pullState,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val s = state) {
                    MyTimeUiState.Loading -> LoadingState()
                    is MyTimeUiState.Error -> ErrorState(
                        message = s.message,
                        onRetry = { vm.refresh() },
                    )
                    is MyTimeUiState.Success -> MyTimeContent(
                        week = s.week,
                        summary = s.summary,
                        onMenuClick = onMenuClick,
                        onPrevWeek = vm::prevWeek,
                        onNextWeek = vm::nextWeek,
                    )
                }
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

/* ----------------------------- content ----------------------------- */

@Composable
private fun MyTimeContent(
    week: WeekRange,
    summary: TimeSummary,
    onMenuClick: () -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        HeroHeader(
            title = "Mój czas",
            topBar = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OnDarkIconBtn(
                        icon = Icons.Outlined.Menu,
                        contentDescription = "Menu",
                        onClick = onMenuClick,
                    )
                    Spacer(Modifier.weight(1f))
                    OnDarkIconBtn(
                        icon = Icons.Outlined.MoreVert,
                        contentDescription = "Więcej",
                        onClick = {},
                    )
                }
            },
        ) {
            Spacer(Modifier.height(dimens.spacing4))
            Text(
                text = "Karta pracy: czas + dojazd + km",
                style = CrmTheme.type.body.copy(color = palette.onDarkMuted),
            )
        }

        Spacer(Modifier.height(dimens.spacing16))

        // 2. Week selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
        ) {
            WeekSelector(
                label = week.label,
                onPrev = onPrevWeek,
                onNext = onNextWeek,
            )
        }

        Spacer(Modifier.height(dimens.spacing12))

        // 3. Three-column summary card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
        ) {
            WeekSummaryCard(
                hours = summary.totalHours,
                travelHours = summary.totalTravelHours,
                kilometers = summary.totalKilometers,
            )
        }

        Spacer(Modifier.height(dimens.spacing16))

        // 4. Bar chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
        ) {
            SectionCard(label = "Rozkład tygodnia") {
                WeekBarChart(week = week, entries = summary.entries)
            }
        }

        Spacer(Modifier.height(dimens.spacing16))

        // 5. Days with entries
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing16),
        ) {
            SectionCard(label = "Dni z wpisami") {
                val daysWithHours = summary.entries
                    .filter { it.hours > 0.0 }
                    .sortedBy { it.date }
                if (daysWithHours.isEmpty()) {
                    Text(
                        text = "Brak zarejestrowanego czasu w tym tygodniu.",
                        style = CrmTheme.type.body.copy(color = palette.muted),
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
                        daysWithHours.forEach { d ->
                            RowCard(
                                title = formatDayPolish(d.date),
                                meta = "${formatHours(d.hours)}h pracy · " +
                                    "${formatHours(d.travelHours)}h dojazdu",
                                trailingValue = "${formatKm(d.kilometers)} km",
                                trailingLabel = null,
                            )
                        }
                    }
                }
            }
        }

        // Bottom spacer so content scrolls clear of the BottomCta gradient.
        Spacer(Modifier.height(dimens.spacing32 + dimens.spacing32))
    }
}

/* ----------------------------- pieces ----------------------------- */

@Composable
private fun WeekSelector(
    label: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius14))
            .background(palette.surface)
            .border(
                width = dimens.borderThin,
                color = palette.line,
                shape = RoundedCornerShape(dimens.radius14),
            )
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WeekArrowBtn(
            icon = Icons.Outlined.ChevronLeft,
            contentDescription = "Poprzedni tydzień",
            onClick = onPrev,
        )
        Text(
            text = label,
            style = CrmTheme.type.mono.copy(
                color = palette.ink,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.weight(1f),
        )
        WeekArrowBtn(
            icon = Icons.Outlined.ChevronRight,
            contentDescription = "Następny tydzień",
            onClick = onNext,
        )
    }
}

@Composable
private fun WeekArrowBtn(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.surface100)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.ink,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun WeekSummaryCard(
    hours: Double,
    travelHours: Double,
    kilometers: Double,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    val labelColor = palette.onPrimary.copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius22))
            .background(palette.primary)
            .padding(dimens.spacing18),
    ) {
        SummaryCell(
            value = "${formatHours(hours)}h",
            label = "Praca",
            valueColor = palette.onPrimary,
            labelColor = labelColor,
            modifier = Modifier.weight(1f),
        )
        SummaryCell(
            value = "${formatHours(travelHours)}h",
            label = "Dojazd",
            valueColor = palette.onPrimary,
            labelColor = labelColor,
            modifier = Modifier.weight(1f),
        )
        SummaryCell(
            value = "${formatKm(kilometers)} km",
            label = "Kilometry",
            valueColor = palette.onPrimary,
            labelColor = labelColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCell(
    value: String,
    label: String,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = CrmTheme.type.label.copy(color = labelColor),
        )
        Spacer(Modifier.height(CrmTheme.dimens.spacing4))
        Text(
            text = value,
            style = CrmTheme.type.display.copy(
                color = valueColor,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            ),
        )
    }
}

@Composable
private fun WeekBarChart(
    week: WeekRange,
    entries: List<TimeSummaryDay>,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    val today = LocalDate.now()

    // Build a Mon..Sun aligned array of hour values.
    val byDate: Map<LocalDate, TimeSummaryDay> = entries.associateBy { it.date }
    val days: List<Pair<LocalDate, Double>> = (0L..6L).map { offset ->
        val d = week.start.plusDays(offset)
        d to (byDate[d]?.hours ?: 0.0)
    }
    val maxHours = days.maxOf { it.second }

    if (maxHours <= 0.0) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Brak zarejestrowanego czasu",
                style = CrmTheme.type.body.copy(color = palette.muted),
            )
        }
        return
    }

    val measurer = rememberTextMeasurer()
    val dayLabelStyle = TextStyle(
        fontFamily = CrmTheme.type.label.fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        color = palette.muted,
    )
    val valueLabelStyle = TextStyle(
        fontFamily = CrmTheme.type.caption.fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        color = palette.ink,
    )
    val barColor = palette.primary
    val todayColor = palette.primaryDeep

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
    ) {
        val gapPx = 6.dp.toPx()
        val labelStripPx = 28.dp.toPx() // bottom strip for two-line labels
        val barAreaHeight = size.height - labelStripPx
        val totalGap = gapPx * 6f
        val barWidth = (size.width - totalGap) / 7f

        days.forEachIndexed { i, (date, hours) ->
            val ratio = (hours / maxHours).coerceIn(0.0, 1.0).toFloat()
            val barH = ratio * barAreaHeight
            val left = i * (barWidth + gapPx)
            val top = barAreaHeight - barH
            val color: Color = if (date == today) todayColor else barColor

            // Bar
            drawRoundedBar(
                left = left,
                top = top,
                width = barWidth,
                height = barH,
                color = color,
            )

            // Day label (Pn / Wt / ...)
            val dayLabel = polishDayShort(date.dayOfWeek)
            val dayLayout = measurer.measure(
                text = AnnotatedString(dayLabel),
                style = dayLabelStyle,
            )
            drawText(
                textLayoutResult = dayLayout,
                topLeft = Offset(
                    x = left + (barWidth - dayLayout.size.width) / 2f,
                    y = barAreaHeight + 2.dp.toPx(),
                ),
            )

            // Value label
            val valueLabel = if (hours > 0.0) "${formatHours(hours)}h" else "—"
            val valueLayout = measurer.measure(
                text = AnnotatedString(valueLabel),
                style = valueLabelStyle,
            )
            drawText(
                textLayoutResult = valueLayout,
                topLeft = Offset(
                    x = left + (barWidth - valueLayout.size.width) / 2f,
                    y = barAreaHeight + dayLayout.size.height + 4.dp.toPx(),
                ),
            )
        }
    }
}

private fun DrawScope.drawRoundedBar(
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    color: Color,
) {
    if (height <= 0f || width <= 0f) return
    drawRect(
        color = color,
        topLeft = Offset(left, top),
        size = Size(width, height),
    )
}

@Composable
private fun OnDarkIconBtn(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    val palette = CrmTheme.colors
    Box(
        modifier = Modifier
            .size(CrmTheme.dimens.hamSize)
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .background(palette.onDarkGlass)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = palette.onDark,
            modifier = Modifier.size(20.dp),
        )
    }
}

/* ----------------------------- helpers ----------------------------- */

private val PL_DAYS_SHORT = mapOf(
    DayOfWeek.MONDAY to "Pn",
    DayOfWeek.TUESDAY to "Wt",
    DayOfWeek.WEDNESDAY to "Śr",
    DayOfWeek.THURSDAY to "Cz",
    DayOfWeek.FRIDAY to "Pt",
    DayOfWeek.SATURDAY to "Sb",
    DayOfWeek.SUNDAY to "Nd",
)

private val PL_MONTHS_LOWER = arrayOf(
    "sty", "lut", "mar", "kwi", "maj", "cze",
    "lip", "sie", "wrz", "paź", "lis", "gru"
)

private fun polishDayShort(day: DayOfWeek): String =
    PL_DAYS_SHORT[day] ?: day.name.take(2)

private fun formatDayPolish(date: LocalDate): String {
    val day = polishDayShort(date.dayOfWeek)
    val month = PL_MONTHS_LOWER[date.monthValue - 1]
    return "$day, ${date.dayOfMonth} $month"
}

private fun formatHours(value: Double): String {
    if (value == 0.0) return "0"
    // 1 decimal place if non-integer, no decimals otherwise.
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(java.util.Locale.ROOT, "%.1f", value)
}

private fun formatKm(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString()
    else String.format(java.util.Locale.ROOT, "%.1f", value)
}

package com.ossadkowski.crm.mobile.ui.serwis.screens.workcard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ossadkowski.crm.mobile.ui.serwis.components.BottomCta
import com.ossadkowski.crm.mobile.ui.serwis.components.LiveTimerState
import com.ossadkowski.crm.mobile.ui.serwis.components.PhoneFrame
import com.ossadkowski.crm.mobile.ui.serwis.components.SectionCard
import com.ossadkowski.crm.mobile.ui.serwis.components.SegmentedToggle
import com.ossadkowski.crm.mobile.ui.serwis.components.SummaryCard
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.ErrorState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.LoadingState
import com.ossadkowski.crm.mobile.ui.serwis.screens.common.TopBarLight
import com.ossadkowski.crm.mobile.ui.serwis.theme.CrmTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun WorkCardScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    vm: WorkCardViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Navigate away on Saved
    LaunchedEffect(state) {
        if (state is WorkCardUiState.Saved) {
            scope.launch { snackbar.showSnackbar("Zapisano kartę pracy") }
            onSaved()
        }
    }

    PhoneFrame(
        topBar = {
            val orderNum = (state as? WorkCardUiState.Editing)?.form?.orderNum ?: ""
            TopBarLight(
                title = "Nowy Wpis Czasu",
                subtitle = orderNum,
                onBack = onBack,
            )
        },
        bottomBar = {
            val canSave = vm.canSave()
            BottomCta(
                label = "Zapisz Kartę Pracy",
                icon = Icons.Outlined.Save,
                enabled = canSave && state is WorkCardUiState.Editing,
                onClick = { vm.save() },
            )
        },
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            when (val s = state) {
                is WorkCardUiState.Editing -> WorkCardForm(
                    form = s.form,
                    vm = vm,
                )
                WorkCardUiState.Saving -> LoadingState()
                WorkCardUiState.Saved -> LoadingState()
                is WorkCardUiState.Error -> ErrorState(
                    message = s.message,
                    onRetry = { /* go back to editing */ },
                )
            }
            SnackbarHost(
                hostState = snackbar,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun WorkCardForm(
    form: WorkCardFormState,
    vm: WorkCardViewModel,
) {
    val dimens = CrmTheme.dimens

    // Tickers for stoper
    LaunchedEffect(form.workTimerState) {
        while (form.workTimerState == LiveTimerState.RUNNING) {
            delay(1000)
            vm.tickWorkTimer(1)
        }
    }
    LaunchedEffect(form.travelTimerState) {
        while (form.travelTimerState == LiveTimerState.RUNNING) {
            delay(1000)
            vm.tickTravelTimer(1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.spacing16, vertical = dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
    ) {
        // 2. Summary card
        SummaryCard(
            leftLabel = "Czas pracy (netto)",
            leftValue = formatHhMm(computeWorkNetSeconds(form)),
            rightLabel = "Suma km",
            rightValue = (totalKm(form)?.toInt()?.toString() ?: "0") + " km",
        )

        // 3. CZAS PRACY
        SectionCard(
            label = "Czas pracy",
            icon = Icons.Outlined.Schedule,
            trailing = {
                SegmentedToggle(
                    options = listOf("Ręczny", "Stoper"),
                    selectedIndex = form.workMode.ordinal,
                    onSelect = { idx -> vm.setWorkMode(Mode.values()[idx]) },
                )
            },
        ) {
            when (form.workMode) {
                Mode.MANUAL -> ManualWorkBlock(form, vm)
                Mode.STOPER -> StoperBlock(
                    seconds = form.workTimerSeconds,
                    state = form.workTimerState,
                    onStart = { vm.startWorkTimer() },
                    onPause = { vm.pauseWorkTimer() },
                    onStop = { vm.stopWorkTimer() },
                )
            }
        }

        // 4. CZAS DOJAZDU
        SectionCard(
            label = "Czas dojazdu",
            icon = Icons.Outlined.Schedule,
            trailing = {
                SegmentedToggle(
                    options = listOf("Ręczny", "Stoper"),
                    selectedIndex = form.travelMode.ordinal,
                    onSelect = { idx -> vm.setTravelMode(Mode.values()[idx]) },
                )
            },
        ) {
            when (form.travelMode) {
                Mode.MANUAL -> ManualTravelBlock(form, vm)
                Mode.STOPER -> StoperBlock(
                    seconds = form.travelTimerSeconds,
                    state = form.travelTimerState,
                    onStart = { vm.startTravelTimer() },
                    onPause = { vm.pauseTravelTimer() },
                    onStop = { vm.stopTravelTimer() },
                )
            }
        }

        // 5. PRZERWY
        SectionCard(
            label = "Przerwy",
            trailing = {
                AddTextBtn(label = "+ DODAJ", onClick = {
                    vm.addBreak(BreakRow(minutes = 15, label = "15 min"))
                })
            },
        ) {
            BreakChips(form = form, onRemove = vm::removeBreak)
        }

        // 6. KILOMETRÓWKA
        SectionCard(
            label = "Kilometrówka",
            trailing = {
                SegmentedToggle(
                    options = listOf("Start/Koniec", "Suma"),
                    selectedIndex = form.mileageMode.ordinal,
                    onSelect = { idx -> vm.setMileageMode(MileageMode.values()[idx]) },
                )
            },
        ) {
            MileageBlock(form = form, vm = vm)
        }

        Spacer(Modifier.height(dimens.spacing32))
    }
}

/* ----------------------- subsections ----------------------- */

@Composable
private fun ManualWorkBlock(form: WorkCardFormState, vm: WorkCardViewModel) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    val totalSec = form.activities.sumOf {
        java.time.temporal.ChronoUnit.SECONDS.between(it.start, it.end)
    }

    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
        Row {
            Text(
                text = "Suma czynności".uppercase(),
                style = CrmTheme.type.label.copy(color = palette.muted),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatHhMm(totalSec),
                style = CrmTheme.type.mono.copy(
                    color = palette.ink,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        HorizontalDivider(color = palette.line)

        // Activity list
        form.activities.forEachIndexed { idx, row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.title,
                    style = CrmTheme.type.body.copy(color = palette.ink),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${row.start}–${row.end}",
                    style = CrmTheme.type.mono.copy(color = palette.muted),
                )
                Spacer(Modifier.width(dimens.spacing8))
                Text(
                    text = "✕",
                    style = CrmTheme.type.label.copy(color = palette.bad.text),
                    modifier = Modifier.clickable { vm.removeActivity(idx) },
                )
            }
        }

        // Quick-add demo row: "Wymiana noża 08:00-09:00" — for MVP we add a fixed
        // sample on tap. Real picker dialogs are a follow-up.
        var titleField by remember { mutableStateOf("") }
        var startField by remember { mutableStateOf("08:00") }
        var endField by remember { mutableStateOf("09:00") }

        OutlinedTextField(
            value = titleField,
            onValueChange = { titleField = it },
            placeholder = { Text("Wybierz czynność…", style = CrmTheme.type.body) },
            singleLine = true,
            colors = textFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
            OutlinedTextField(
                value = startField,
                onValueChange = { startField = it },
                label = { Text("ROZPOCZĘCIE", style = CrmTheme.type.label) },
                singleLine = true,
                colors = textFieldColors(),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = endField,
                onValueChange = { endField = it },
                label = { Text("ZAKOŃCZENIE", style = CrmTheme.type.label) },
                singleLine = true,
                colors = textFieldColors(),
                modifier = Modifier.weight(1f),
            )
        }
        if (form.activityError != null) {
            Text(
                text = form.activityError,
                style = CrmTheme.type.label.copy(color = palette.bad.text),
            )
        }

        // + DODAJ CZYNNOŚĆ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius14))
                .border(
                    width = dimens.borderMed,
                    color = palette.primary,
                    shape = RoundedCornerShape(dimens.radius14),
                )
                .clickable {
                    val s = parseTime(startField) ?: return@clickable
                    val e = parseTime(endField) ?: return@clickable
                    vm.addActivity(
                        ActivityRow(
                            title = titleField.ifBlank { "Czynność" },
                            start = s,
                            end = e,
                        ),
                    )
                    titleField = ""
                }
                .padding(vertical = dimens.spacing12),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "+ DODAJ CZYNNOŚĆ",
                style = CrmTheme.type.label.copy(color = palette.primaryDeep),
            )
        }
    }
}

@Composable
private fun ManualTravelBlock(form: WorkCardFormState, vm: WorkCardViewModel) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    var startField by remember { mutableStateOf(form.travelStart?.toString() ?: "") }
    var endField by remember { mutableStateOf(form.travelEnd?.toString() ?: "") }

    Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
            OutlinedTextField(
                value = startField,
                onValueChange = { v ->
                    startField = v
                    vm.setTravelStart(parseTime(v))
                },
                label = { Text("ROZPOCZĘCIE", style = CrmTheme.type.label) },
                singleLine = true,
                colors = textFieldColors(),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = endField,
                onValueChange = { v ->
                    endField = v
                    vm.setTravelEnd(parseTime(v))
                },
                label = { Text("ZAKOŃCZENIE", style = CrmTheme.type.label) },
                singleLine = true,
                colors = textFieldColors(),
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = "Format HH:MM (24h)",
            style = CrmTheme.type.label.copy(color = palette.muted),
        )
    }
}

@Composable
private fun StoperBlock(
    seconds: Long,
    state: LiveTimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onStop: () -> Unit,
) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spacing8),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = formatHhMmSs(seconds),
            style = CrmTheme.type.display.copy(
                color = palette.ink,
                fontWeight = FontWeight.Bold,
            ),
        )
        val label = when (state) {
            LiveTimerState.IDLE -> "STOPER GOTOWY"
            LiveTimerState.RUNNING -> "STOPER AKTYWOWANY"
            LiveTimerState.PAUSED -> "WSTRZYMANY"
            LiveTimerState.DONE -> "ZAKOŃCZONY"
        }
        Text(
            text = label,
            style = CrmTheme.type.label.copy(color = palette.muted),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
            StoperBtn(
                text = if (state == LiveTimerState.RUNNING) "Wstrzymaj" else "Start",
                onClick = if (state == LiveTimerState.RUNNING) onPause else onStart,
                isStart = state != LiveTimerState.RUNNING,
            )
            StoperBtn(
                text = "Stop",
                onClick = onStop,
                isStart = false,
            )
        }
    }
}

@Composable
private fun StoperBtn(text: String, onClick: () -> Unit, isStart: Boolean) {
    val palette = CrmTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius14))
            .background(if (isStart) palette.primary else palette.surface100)
            .clickable(onClick = onClick)
            .padding(horizontal = CrmTheme.dimens.spacing20, vertical = CrmTheme.dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isStart) Icons.Outlined.PlayArrow else Icons.Outlined.Stop,
            contentDescription = null,
            tint = if (isStart) palette.onPrimary else palette.ink,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(CrmTheme.dimens.spacing4))
        Text(
            text = text,
            style = CrmTheme.type.label.copy(
                color = if (isStart) palette.onPrimary else palette.ink,
            ),
        )
    }
}

@Composable
private fun BreakChips(form: WorkCardFormState, onRemove: (Int) -> Unit) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    if (form.breaks.isEmpty()) {
        Text(
            text = "Brak przerw",
            style = CrmTheme.type.body.copy(color = palette.muted),
        )
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing6)) {
        form.breaks.forEachIndexed { idx, br ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimens.radius14))
                    .background(palette.surface100)
                    .clickable { onRemove(idx) }
                    .padding(horizontal = dimens.spacing12, vertical = dimens.spacing8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${br.minutes} min",
                    style = CrmTheme.type.label.copy(color = palette.ink),
                )
            }
        }
    }
}

@Composable
private fun MileageBlock(form: WorkCardFormState, vm: WorkCardViewModel) {
    val palette = CrmTheme.colors
    val dimens = CrmTheme.dimens
    when (form.mileageMode) {
        MileageMode.START_END -> {
            var s by remember { mutableStateOf(form.mileageStart?.toString() ?: "") }
            var e by remember { mutableStateOf(form.mileageEnd?.toString() ?: "") }
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
                Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacing8)) {
                    OutlinedTextField(
                        value = s,
                        onValueChange = { v ->
                            s = v.filter { it.isDigit() }
                            vm.setMileageStart(s.toIntOrNull())
                        },
                        label = { Text("START", style = CrmTheme.type.label) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = e,
                        onValueChange = { v ->
                            e = v.filter { it.isDigit() }
                            vm.setMileageEnd(e.toIntOrNull())
                        },
                        label = { Text("KONIEC", style = CrmTheme.type.label) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = textFieldColors(),
                        modifier = Modifier.weight(1f),
                    )
                }
                val km = totalKm(form)
                Text(
                    text = "Auto-kalkulacja: ${km?.toInt() ?: 0} km",
                    style = CrmTheme.type.mono.copy(color = palette.primaryDeep),
                )
                if (form.mileageError != null) {
                    Text(
                        text = form.mileageError,
                        style = CrmTheme.type.label.copy(color = palette.bad.text),
                    )
                }
            }
        }
        MileageMode.SUM -> {
            var v by remember { mutableStateOf(form.mileageSum?.toString() ?: "") }
            OutlinedTextField(
                value = v,
                onValueChange = { x ->
                    v = x.filter { c -> c.isDigit() || c == '.' }
                    vm.setMileageSum(v.toDoubleOrNull())
                },
                label = { Text("SUMA KM", style = CrmTheme.type.label) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = { Text(" km", style = CrmTheme.type.body) },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AddTextBtn(label: String, onClick: () -> Unit) {
    val palette = CrmTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CrmTheme.dimens.radius10))
            .clickable(onClick = onClick)
            .padding(horizontal = CrmTheme.dimens.spacing8, vertical = CrmTheme.dimens.spacing4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Add,
            contentDescription = null,
            tint = palette.primaryDeep,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            style = CrmTheme.type.label.copy(color = palette.primaryDeep),
        )
    }
}

@Composable
private fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = CrmTheme.colors.surface,
    unfocusedContainerColor = CrmTheme.colors.surface,
    focusedIndicatorColor = CrmTheme.colors.primary,
    unfocusedIndicatorColor = CrmTheme.colors.line,
)

private fun parseTime(s: String): LocalTime? = try {
    val parts = s.split(":")
    val h = parts.getOrNull(0)?.toIntOrNull() ?: return null
    val m = parts.getOrNull(1)?.toIntOrNull() ?: return null
    LocalTime.of(h, m)
} catch (_: Exception) {
    null
}

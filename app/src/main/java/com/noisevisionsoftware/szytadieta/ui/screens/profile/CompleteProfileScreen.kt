package com.noisevisionsoftware.szytadieta.ui.screens.profile

import android.icu.util.Calendar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.Gender
import com.noisevisionsoftware.szytadieta.ui.common.UiEventHandler

@Composable
fun CompleteProfileScreen(
    profileViewModel: ProfileViewModel = hiltViewModel(),
    onSkip: () -> Unit,
    isLoading: Boolean = false
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ProfileContent(
            selectedDate = selectedDate,
            selectedGender = selectedGender,
            isLoading = isLoading,
            onDateClick = { showDatePicker = true },
            onGenderSelect = { selectedGender = it },
            onSkip = onSkip,
            onSave = {
                selectedDate?.let { date ->
                    selectedGender?.let { gender ->
                        profileViewModel.setTempBirthDate(date)
                        profileViewModel.setTempGender(gender)
                        profileViewModel.saveProfile()
                    }
                }
            }
        )

        UiEventHandler(
            uiEvent = profileViewModel.uiEvent,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }

    if (showDatePicker) {
        ProfileDatePicker(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun ProfileContent(
    selectedDate: Long?,
    selectedGender: Gender?,
    isLoading: Boolean,
    onDateClick: () -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileHeader()
        ProfileCard(
            selectedDate = selectedDate,
            selectedGender = selectedGender,
            isLoading = isLoading,
            onDateClick = onDateClick,
            onGenderSelect = onGenderSelect,
            onSkip = onSkip,
            onSave = onSave
        )
    }
}

@Composable
private fun ProfileHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Witaj!",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 48.dp)
        )

        Text(
            text = "Pomóż nam lepiej Cię poznać",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun ProfileCard(
    selectedDate: Long?,
    selectedGender: Gender?,
    isLoading: Boolean,
    onDateClick: () -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DateSelector(
                selectedDate = selectedDate,
                onDateClick = onDateClick
            )

            GenderSelector(
                selectedGender = selectedGender,
                onGenderSelect = onGenderSelect
            )

            ActionButtons(
                isEnabled = selectedDate != null && selectedGender != null,
                isLoading = isLoading,
                onSkip = onSkip,
                onSave = onSave
            )
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: Long?,
    onDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Data urodzenia",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedButton(
            onClick = onDateClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                if (selectedDate != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                text = selectedDate?.let {
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = it
                    }
                    "${calendar.get(Calendar.DAY_OF_MONTH)}.${calendar.get(Calendar.MONTH) + 1}.${
                        calendar.get(
                            Calendar.YEAR
                        )
                    }"
                } ?: "Wybierz datę urodzenia",
                color = if (selectedDate != null)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GenderSelector(
    selectedGender: Gender?,
    onGenderSelect: (Gender) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Płeć",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Gender.entries.forEach { gender ->
                GenderChip(
                    gender = gender,
                    isSelected = selectedGender == gender,
                    onSelect = { onGenderSelect(gender) }
                )
            }
        }
    }
}

@Composable
private fun GenderChip(
    gender: Gender,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = {
            Text(
                text = when (gender) {
                    Gender.MALE -> "Mężczyzna"
                    Gender.FEMALE -> "Kobieta"
                    Gender.OTHER -> "Inna"
                },
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline,
            borderWidth = 1.dp,
            enabled = true,
            selected = isSelected
        ),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ActionButtons(
    isEnabled: Boolean,
    isLoading: Boolean,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onSkip,
            enabled = !isLoading
        ) {
            Text(
                "Uzupełnię później",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onSave,
            enabled = isEnabled && !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Zapisz")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDatePicker(
    initialDate: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let {
                    onDateSelected(it)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
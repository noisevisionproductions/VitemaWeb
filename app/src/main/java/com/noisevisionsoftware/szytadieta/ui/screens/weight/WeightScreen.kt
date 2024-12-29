package com.noisevisionsoftware.szytadieta.ui.screens.weight

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.Weight
import com.noisevisionsoftware.szytadieta.ui.common.UiEventHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val weightState by viewModel.weightState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { WeightTopBar(onBackClick = onBackClick) },
        floatingActionButton = {
            AddWeightFAB(onClick = { showAddDialog = true })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WeightContent(
                weightState = weightState,
                onDeleteClick = viewModel::deleteWeight
            )

            if (showAddDialog) {
                AddWeightDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { weight, note ->
                        viewModel.addWeight(weight = weight, note = note)
                        showAddDialog = false
                    }
                )
            }

            UiEventHandler(
                uiEvent = viewModel.uiEvent,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text("Historia wagi") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Wróć"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun AddWeightFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Default.Add, "Dodaj wagę")
    }
}

@Composable
private fun WeightContent(
    weightState: WeightViewModel.WeightState,
    onDeleteClick: (String) -> Unit
) {
    AnimatedContent(
        targetState = weightState,
        transitionSpec = {
            (fadeIn() + slideInVertically()).togetherWith(fadeOut() + slideOutVertically())
        }, label = ""
    ) { state ->
        when (state) {
            WeightViewModel.WeightState.Loading -> WeightLoadingIndicator()
            is WeightViewModel.WeightState.Success -> WeightList(
                weights = state.weights,
                onDeleteClick = onDeleteClick
            )

            is WeightViewModel.WeightState.Error -> WeightError(state.exception.message)
            WeightViewModel.WeightState.Initial -> Unit
        }
    }
}

@Composable
private fun WeightLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun WeightError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun WeightList(
    weights: List<Weight>,
    onDeleteClick: (String) -> Unit
) {
    if (weights.isEmpty()) {
        EmptyWeightList()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                WeightStats(weights = weights)
            }

            items(
                items = weights,
                key = { weight -> weight.id }
            ) { weight ->
                WeightItem(
                    weight = weight,
                    onDeleteClick = { onDeleteClick(weight.id) }
                )
            }
        }
    }
}

@Composable
private fun WeightStats(weights: List<Weight>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Statystyki",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeightStatItem(
                    title = "Ostatni pomiar",
                    value = weights.firstOrNull()?.let { "${it.weight} kg" } ?: "-"
                )
                WeightStatItem(
                    title = "Średnia waga",
                    value = weights.takeIf { it.isNotEmpty() }
                        ?.let { "%.1f kg".format(it.map { w -> w.weight }.average()) }
                        ?: "-"
                )
                WeightStatItem(
                    title = "Liczba pomiarów",
                    value = weights.size.toString()
                )
            }

            if (weights.size >= 2) {
                val firstWeight = weights.last().weight
                val lastWeight = weights.first().weight
                val difference = lastWeight - firstWeight
                val differenceText = when {
                    difference > 0 -> "+%.1f kg".format(difference)
                    difference < 0 -> "%.1f kg".format(difference)
                    else -> "0 kg"
                }

                Text(
                    text = "Zmiana: $differenceText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        difference > 0 -> MaterialTheme.colorScheme.error
                        difference < 0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
fun WeightStatItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyWeightList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Scale,
                contentDescription = "Waga",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak pomiarów wagi",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dodaj swój pierwszy pomiar używając przycisku poniżej",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightItem(
    weight: Weight,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${weight.weight} kg",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                        .format(Date(weight.date)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (weight.note.isNotBlank()) {
                    Text(
                        text = weight.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń wpis",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDeleteDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Potwierdź usunięcie",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Czy na pewno chcesz usunąć ten wpis?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Anuluj")
                        }
                        TextButton(
                            onClick = {
                                onDeleteClick()
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Usuń")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Dodaj nowy pomiar",
                    style = MaterialTheme.typography.headlineSmall
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            hasError = false
                        },
                        label = { Text("Waga (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = hasError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = if (hasError) {
                            { Text("Wprowadź prawidłową wagę") }
                        } else null
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notatka (opcjonalnie)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Anuluj")
                    }
                    TextButton(
                        onClick = {
                            try {
                                val weight = weightText.replace(",", ".").toDouble()
                                if (weight > 0) {
                                    onConfirm(weight, note)
                                } else {
                                    hasError = true
                                }
                            } catch (e: NumberFormatException) {
                                hasError = true
                            }
                        }
                    ) {
                        Text("Dodaj")
                    }
                }
            }
        }
    }
}
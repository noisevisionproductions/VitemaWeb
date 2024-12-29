package com.noisevisionsoftware.szytadieta.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Błąd",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Zamknij",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedErrorDialog(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        message?.let {
            ErrorDialog(
                message = it,
                onDismiss = onDismiss,
                modifier = modifier
            )
        }
    }
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Sukces",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Zamknij",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AnimatedSuccessDialog(
    message: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        message?.let {
            SuccessDialog(
                message = it,
                onDismiss = onDismiss,
                modifier = modifier
            )
        }
    }
}

@Composable
fun UiEventHandler(
    uiEvent: StateFlow<UiEvent?>,
    modifier: Modifier = Modifier,
    autoHideDelay: Long = 3000L,
    onSuccess: (suspend (String) -> Unit)? = null,
    onError: (suspend (String) -> Unit)? = null,
) {
    var localEvent by remember { mutableStateOf<UiEvent?>(null) }
    val currentEvent by uiEvent.collectAsState()
    var lastEventKey by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose {
            localEvent = null
            lastEventKey = 0
        }
    }

    LaunchedEffect(currentEvent, lastEventKey) {
        localEvent = currentEvent
        when (currentEvent) {
            is UiEvent.ShowError -> {
                onError?.invoke((currentEvent as UiEvent.ShowError).message)
            }

            is UiEvent.ShowSuccess -> {
                onSuccess?.invoke((currentEvent as UiEvent.ShowSuccess).message)
            }

            null -> {}
        }

        if (currentEvent != null) {
            delay(autoHideDelay)
            if (localEvent == currentEvent) {
                localEvent = null
            }
        }
    }

    LaunchedEffect(currentEvent) {
        if (currentEvent != null) {
            lastEventKey++
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedErrorDialog(
            message = when (localEvent) {
                is UiEvent.ShowError -> (localEvent as UiEvent.ShowError).message
                else -> null
            },
            onDismiss = {
                localEvent = null
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        AnimatedSuccessDialog(
            message = when (localEvent) {
                is UiEvent.ShowSuccess -> (localEvent as UiEvent.ShowSuccess).message
                else -> null
            },
            onDismiss = {
                localEvent = null
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
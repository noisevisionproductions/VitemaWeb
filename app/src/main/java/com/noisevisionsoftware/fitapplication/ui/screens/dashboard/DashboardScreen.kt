package com.noisevisionsoftware.fitapplication.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun DashboardScreen(
    onMealPlanClick: () -> Unit = {},
    onCaloriesTrackerClick: () -> Unit = {},
    onWaterTrackerClick: () -> Unit = {},
    onRecipesClick: () -> Unit = {},
    onProgressClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        TopBar()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            item {
                DashboardCard(
                    title = "Plan posiłków",
                    icon = Icons.Default.RestaurantMenu,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = onMealPlanClick
                )
            }
            item {
                DashboardCard(
                    title = "Licznik kalorii",
                    icon = Icons.Default.MonitorWeight,
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    onClick = onCaloriesTrackerClick
                )
            }
            item {
                DashboardCard(
                    title = "Nawodnienie",
                    icon = Icons.Default.WaterDrop,
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onWaterTrackerClick
                )
            }
            item {
                DashboardCard(
                    title = "Przepisy",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = onRecipesClick
                )
            }
            item {
                DashboardCard(
                    title = "Postępy",
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    onClick = onProgressClick
                )
            }
            item {
                DashboardCard(
                    title = "Ustawienia",
                    icon = Icons.Default.Settings,
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
fun TopBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Witaj!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Co dzisiaj zjemy?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(
            onClick = {}
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profil",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

package com.agon.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.AreaChart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Height
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.data.SurveyCalculator
import com.agon.app.ui.Format
import com.agon.app.ui.Strings
import com.agon.app.ui.accuracyLevel
import com.agon.app.ui.accuracyText
import com.agon.app.ui.components.ChartSurface
import com.agon.app.ui.components.ElevationChart
import com.agon.app.ui.components.PointRow
import com.agon.app.ui.components.SectionHeader
import com.agon.app.ui.components.StatCard
import com.agon.app.viewmodel.SurveyUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    state: SurveyUiState,
    strings: Strings,
    onAddPoint: () -> Unit,
    onOpenPoints: () -> Unit,
    onOpenTraverse: () -> Unit,
    onExport: () -> Unit,
    onOpenPoint: (String) -> Unit,
) {
    val decimals = state.settings.decimals
    val units = state.settings.units
    val order = state.traverseOrder
    val traverse = remember(order, state.traverseClosed) {
        SurveyCalculator.traverse(order, state.traverseClosed)
    }
    val elevationStats = remember(state.points) { SurveyCalculator.elevationStats(state.points) }
    val accuracy = accuracyLevel(traverse.precisionDenominator, traverse.isClosed)
    val accuracyAccent = when (accuracy) {
        com.agon.app.ui.AccuracyLevel.EXCELLENT -> MaterialTheme.colorScheme.primary
        com.agon.app.ui.AccuracyLevel.ACCEPTABLE -> MaterialTheme.colorScheme.secondary
        com.agon.app.ui.AccuracyLevel.POOR -> MaterialTheme.colorScheme.error
        com.agon.app.ui.AccuracyLevel.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val recent = remember(state.points) { state.points.sortedByDescending { it.recordedAt }.take(4) }
    val locale = if (state.settings.language.name == "ARABIC") Locale("ar") else Locale.US
    val today = remember(state.points) {
        SimpleDateFormat("EEEE d MMMM", locale).format(Date(System.currentTimeMillis()))
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF0D5F5A), Color(0xFF2F4E3C)),
                        )
                    )
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = strings.tagline,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.78f),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Explore,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.16f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.settings.projectName.ifBlank { strings.projectLabel },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = today,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = Icons.Outlined.AddLocationAlt,
                    label = strings.statPoints,
                    value = state.points.size.toString(),
                    accent = MaterialTheme.colorScheme.primary,
                    caption = "${strings.pointsUnit}",
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Outlined.Straighten,
                    label = strings.statLength,
                    value = Format.distance(traverse.perimeter, units, decimals),
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    icon = Icons.Outlined.Route,
                    label = strings.statAccuracy,
                    value = if (traverse.isClosed && traverse.precisionDenominator.isFinite()) {
                        "1:${traverse.precisionDenominator.toInt()}"
                    } else {
                        "—"
                    },
                    accent = accuracyAccent,
                    caption = strings.accuracyText(accuracy),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    icon = Icons.Outlined.AreaChart,
                    label = strings.statArea,
                    value = Format.area(traverse.area, units),
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            ChartSurface {
                SectionHeader(
                    title = strings.elevationProfile,
                    trailing = {
                        Text(
                            text = strings.elevationProfileHint,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (state.points.size >= 2) {
                    ElevationChart(
                        points = order,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = strings.noPointsBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryChip(
                        icon = Icons.Outlined.Height,
                        label = strings.averageElevation,
                        value = Format.number(elevationStats.third, decimals),
                        modifier = Modifier.weight(1f),
                    )
                    SummaryChip(
                        icon = Icons.Outlined.Explore,
                        label = strings.elevationRange,
                        value = "${Format.number(elevationStats.first, decimals)} – ${Format.number(elevationStats.second, decimals)}",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        item {
            SectionHeader(title = strings.quickActions)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(
                    icon = Icons.Outlined.AddLocationAlt,
                    label = strings.actionAddPoint,
                    onClick = onAddPoint,
                    modifier = Modifier.weight(1f),
                )
                QuickAction(
                    icon = Icons.Outlined.Route,
                    label = strings.actionTraverse,
                    onClick = onOpenTraverse,
                    modifier = Modifier.weight(1f),
                )
                QuickAction(
                    icon = Icons.Outlined.AreaChart,
                    label = strings.actionExport,
                    onClick = onExport,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            SectionHeader(
                title = strings.recentPoints,
                trailing = {
                    TextButton(onClick = onOpenPoints) { Text(strings.viewAll) }
                },
            )
        }

        if (recent.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = strings.noPointsTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = strings.noPointsBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column {
                        recent.forEachIndexed { index, point ->
                            PointRow(
                                point = point,
                                strings = strings,
                                units = units,
                                decimals = decimals,
                                onClick = { onOpenPoint(point.id) },
                            )
                            if (index != recent.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
            )
        }
    }
}

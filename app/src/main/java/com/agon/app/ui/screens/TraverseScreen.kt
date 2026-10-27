package com.agon.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.data.SurveyCalculator
import com.agon.app.ui.Format
import com.agon.app.ui.Strings
import com.agon.app.ui.AccuracyLevel
import com.agon.app.ui.accuracyLevel
import com.agon.app.ui.accuracyText
import com.agon.app.ui.components.AccuracyGauge
import com.agon.app.ui.components.ChartSurface
import com.agon.app.ui.components.EmptyState
import com.agon.app.ui.components.MetricPill
import com.agon.app.ui.components.SectionHeader
import com.agon.app.ui.components.TraversePlan
import com.agon.app.viewmodel.SurveyUiState

@Composable
fun TraverseScreen(
    state: SurveyUiState,
    strings: Strings,
    onToggleClosed: (Boolean) -> Unit,
    onReverse: () -> Unit,
    onExportCsv: () -> Unit,
) {
    val decimals = state.settings.decimals
    val units = state.settings.units
    val order = state.traverseOrder
    val closed = state.traverseClosed
    val traverse = remember(order, closed) { SurveyCalculator.traverse(order, closed) }
    val legs = remember(order, closed) { SurveyCalculator.legs(order, closed) }

    if (order.size < 3) {
        EmptyState(
            icon = Icons.Outlined.Route,
            title = strings.needPoints,
            body = strings.needPointsBody,
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }

    val level = accuracyLevel(traverse.precisionDenominator, traverse.isClosed)
    val precisionLabel = strings.accuracyText(level)
    val precisionColor = when (level) {
        AccuracyLevel.EXCELLENT -> MaterialTheme.colorScheme.primary
        AccuracyLevel.ACCEPTABLE -> MaterialTheme.colorScheme.secondary
        AccuracyLevel.POOR -> MaterialTheme.colorScheme.error
        AccuracyLevel.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = closed,
                    onClick = { onToggleClosed(true) },
                    label = { Text(strings.closedLoop) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
                FilterChip(
                    selected = !closed,
                    onClick = { onToggleClosed(false) },
                    label = { Text(strings.openPath) },
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = onReverse,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SwapVert,
                        contentDescription = strings.reverseOrder,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(strings.reverseOrder)
                }
            }
        }

        item {
            ChartSurface {
                SectionHeader(title = strings.plan)
                Spacer(modifier = Modifier.height(12.dp))
                TraversePlan(
                    points = order,
                    closed = closed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricPill(
                    label = strings.perimeter,
                    value = Format.distance(traverse.perimeter, units, decimals),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                MetricPill(
                    label = strings.area,
                    value = Format.area(traverse.area, units),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                MetricPill(
                    label = strings.legCount,
                    value = legs.size.toString(),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AccuracyGauge(
                        denominator = traverse.precisionDenominator,
                        color = precisionColor,
                        caption = strings.precision,
                        modifier = Modifier.size(112.dp),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = precisionLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = precisionColor,
                        )
                        if (closed) {
                            InfoLine(
                                label = strings.closingError,
                                value = Format.distance(traverse.closingError, units, decimals),
                            )
                            InfoLine(
                                label = strings.misclosureBearing,
                                value = Format.bearing(traverse.misclosureBearing),
                            )
                            InfoLine(
                                label = strings.closureVector,
                                value = "ΔE ${Format.number(traverse.closingEast, decimals)} / ΔN ${Format.number(traverse.closingNorth, decimals)}",
                            )
                        } else {
                            Text(
                                text = strings.noClosing,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = strings.legs)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        HeaderCell(strings.from, Modifier.weight(1.1f))
                        HeaderCell(strings.to, Modifier.weight(1.1f))
                        HeaderCell(strings.length, Modifier.weight(1f))
                        HeaderCell(strings.bearing, Modifier.weight(1f))
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    legs.forEachIndexed { index, leg ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Cell(leg.from.code, Modifier.weight(1.1f))
                            Cell(leg.to.code, Modifier.weight(1.1f))
                            Cell(Format.distance(leg.length, units, decimals), Modifier.weight(1f))
                            Cell(
                                text = Format.bearing(leg.bearing),
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (index != legs.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            )
                        }
                    }
                }
            }
        }

        if (closed) {
            item {
                SectionHeader(title = strings.adjusted)
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        traverse.adjusted.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = point.code,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = "E ${Format.number(point.easting, decimals)}   N ${Format.number(point.northing, decimals)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onExportCsv,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(strings.exportCsv)
            }
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
private fun Cell(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = color,
        maxLines = 1,
        modifier = modifier,
    )
}

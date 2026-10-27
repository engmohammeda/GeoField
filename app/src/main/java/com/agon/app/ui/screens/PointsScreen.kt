package com.agon.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.data.PointCategory
import com.agon.app.ui.Strings
import com.agon.app.ui.components.EmptyState
import com.agon.app.ui.components.PointRow
import com.agon.app.ui.forCategory
import com.agon.app.viewmodel.PointSort
import com.agon.app.viewmodel.SurveyUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointsScreen(
    state: SurveyUiState,
    strings: Strings,
    onQueryChange: (String) -> Unit,
    onFilterChange: (PointCategory?) -> Unit,
    onSortChange: (PointSort) -> Unit,
    onDelete: (String) -> Unit,
    onAddPoint: () -> Unit,
    onOpenPoint: (String) -> Unit,
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    val filtered = state.filteredPoints

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = strings.search,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = strings.close,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        FilterChip(
                            selected = state.categoryFilter == null,
                            onClick = { onFilterChange(null) },
                            label = { Text(strings.filterAll) },
                        )
                    }
                    items(PointCategory.entries) { category ->
                        FilterChip(
                            selected = state.categoryFilter == category,
                            onClick = {
                                onFilterChange(
                                    if (state.categoryFilter == category) null else category
                                )
                            },
                            label = { Text(strings.forCategory(category)) },
                        )
                    }
                }
                Box {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = strings.sortBy,
                            tint = if (state.sort != PointSort.TIME) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuOpen,
                        onDismissRequest = { sortMenuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(strings.sortTime) },
                            onClick = {
                                onSortChange(PointSort.TIME)
                                sortMenuOpen = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortCode) },
                            onClick = {
                                onSortChange(PointSort.CODE)
                                sortMenuOpen = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text(strings.sortElevation) },
                            onClick = {
                                onSortChange(PointSort.ELEVATION)
                                sortMenuOpen = false
                            },
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Place,
                    title = strings.emptyTitle,
                    body = if (state.points.isEmpty()) strings.emptyPointsBody else strings.emptyBody,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { point ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    onDelete(point.id)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                                            RoundedCornerShape(20.dp),
                                        )
                                        .padding(horizontal = 24.dp),
                                    contentAlignment = Alignment.CenterEnd,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Delete,
                                        contentDescription = strings.delete,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            },
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            ) {
                                PointRow(
                                    point = point,
                                    strings = strings,
                                    units = state.settings.units,
                                    decimals = state.settings.decimals,
                                    onClick = { onOpenPoint(point.id) },
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddPoint,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = strings.actionAddPoint,
            )
        }
    }
}

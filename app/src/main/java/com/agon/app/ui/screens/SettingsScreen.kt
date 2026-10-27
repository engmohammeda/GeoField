package com.agon.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agon.app.data.AppLanguage
import com.agon.app.data.ThemeMode
import com.agon.app.data.UnitSystem
import com.agon.app.ui.Strings
import com.agon.app.viewmodel.SurveyUiState
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    state: SurveyUiState,
    strings: Strings,
    onThemeChange: (ThemeMode) -> Unit,
    onUnitsChange: (UnitSystem) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onDecimalsChange: (Int) -> Unit,
    onProjectNameChange: (String) -> Unit,
    onExportCsv: () -> Unit,
    onExportGeoJson: () -> Unit,
    onLoadSample: () -> Unit,
    onClearAll: () -> Unit,
) {
    var showClearDialog by remember { mutableStateOf(false) }
    var projectName by remember(state.settings.projectName) {
        mutableStateOf(state.settings.projectName)
    }

    LaunchedEffect(projectName) {
        delay(700)
        if (projectName != state.settings.projectName) onProjectNameChange(projectName)
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = strings.projectName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        placeholder = { Text(strings.projectNameHint) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        item {
            SettingsCard(title = strings.appearance, icon = Icons.Outlined.Palette) {
                SettingLabel(Icons.Outlined.DarkMode, strings.theme)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.settings.themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeChange(ThemeMode.SYSTEM) },
                        label = { Text(strings.themeSystem) },
                    )
                    FilterChip(
                        selected = state.settings.themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeChange(ThemeMode.LIGHT) },
                        label = { Text(strings.themeLight) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.LightMode,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                    FilterChip(
                        selected = state.settings.themeMode == ThemeMode.DARK,
                        onClick = { onThemeChange(ThemeMode.DARK) },
                        label = { Text(strings.themeDark) },
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))

                SettingLabel(Icons.Outlined.Straighten, strings.units)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.settings.units == UnitSystem.METRIC,
                        onClick = { onUnitsChange(UnitSystem.METRIC) },
                        label = { Text(strings.metric) },
                    )
                    FilterChip(
                        selected = state.settings.units == UnitSystem.IMPERIAL,
                        onClick = { onUnitsChange(UnitSystem.IMPERIAL) },
                        label = { Text(strings.imperial) },
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))

                SettingLabel(Icons.Outlined.PhoneAndroid, strings.decimals)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..3).forEach { value ->
                        FilterChip(
                            selected = state.settings.decimals == value,
                            onClick = { onDecimalsChange(value) },
                            label = { Text(value.toString()) },
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(title = strings.language, icon = Icons.Outlined.Language) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.settings.language == AppLanguage.ARABIC,
                        onClick = { onLanguageChange(AppLanguage.ARABIC) },
                        label = { Text(strings.arabic) },
                    )
                    FilterChip(
                        selected = state.settings.language == AppLanguage.ENGLISH,
                        onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                        label = { Text(strings.english) },
                    )
                }
            }
        }

        item {
            SettingsCard(title = strings.dataSection, icon = Icons.Outlined.Publish) {
                SettingsRow(
                    icon = Icons.Outlined.Publish,
                    title = strings.exportCsv,
                    onClick = onExportCsv,
                )
                SettingsRow(
                    icon = Icons.Outlined.Publish,
                    title = strings.exportGeoJson,
                    onClick = onExportGeoJson,
                )
                SettingsRow(
                    icon = Icons.Outlined.Science,
                    title = strings.loadSample,
                    onClick = onLoadSample,
                )
                SettingsRow(
                    icon = Icons.Outlined.DeleteForever,
                    title = strings.clearData,
                    onClick = { showClearDialog = true },
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }

        item {
            SettingsCard(title = strings.about, icon = Icons.Outlined.Info) {
                Text(
                    text = strings.aboutBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.size(12.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.version,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(strings.clearDataTitle) },
            text = { Text(strings.clearDataBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAll()
                    }
                ) {
                    Text(strings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(strings.cancel) }
            },
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.size(14.dp))
            content()
        }
    }
}

@Composable
private fun SettingLabel(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = tint,
        )
    }
}

package com.agon.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.agon.app.data.PointCategory
import com.agon.app.data.SurveyPoint
import com.agon.app.ui.Strings
import com.agon.app.ui.forCategory

data class PointDraft(
    val code: String,
    val easting: Double,
    val northing: Double,
    val elevation: Double,
    val category: PointCategory,
    val description: String,
    val accuracy: Double,
)

/**
 * Bottom sheet used to record a new point or edit an existing one.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PointEditorSheet(
    initial: SurveyPoint?,
    strings: Strings,
    onDismiss: () -> Unit,
    onSave: (PointDraft) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var code by rememberSaveable { mutableStateOf(initial?.code ?: "") }
    var easting by rememberSaveable { mutableStateOf(initial?.easting?.let { formatForField(it) } ?: "") }
    var northing by rememberSaveable { mutableStateOf(initial?.northing?.let { formatForField(it) } ?: "") }
    var elevation by rememberSaveable { mutableStateOf(initial?.elevation?.let { formatForField(it) } ?: "") }
    var accuracy by rememberSaveable { mutableStateOf(initial?.accuracy?.let { formatForField(it) } ?: "") }
    var description by rememberSaveable { mutableStateOf(initial?.description ?: "") }
    var category by rememberSaveable { mutableStateOf(initial?.category ?: PointCategory.CORNER) }

    var codeError by remember { mutableStateOf(false) }
    var eastingError by remember { mutableStateOf(false) }
    var northingError by remember { mutableStateOf(false) }
    var elevationError by remember { mutableStateOf(false) }
    var accuracyError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (initial == null) strings.newPoint else strings.editPoint,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = strings.close,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                    codeError = false
                },
                label = { Text(strings.fieldCode) },
                singleLine = true,
                isError = codeError,
                supportingText = if (codeError) {
                    { Text(strings.requiredField) }
                } else null,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = easting,
                    onValueChange = {
                        easting = it
                        eastingError = false
                    },
                    label = { Text(strings.fieldEasting) },
                    singleLine = true,
                    isError = eastingError,
                    supportingText = if (eastingError) {
                        { Text(strings.invalidNumber) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = northing,
                    onValueChange = {
                        northing = it
                        northingError = false
                    },
                    label = { Text(strings.fieldNorthing) },
                    singleLine = true,
                    isError = northingError,
                    supportingText = if (northingError) {
                        { Text(strings.invalidNumber) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = elevation,
                    onValueChange = {
                        elevation = it
                        elevationError = false
                    },
                    label = { Text(strings.fieldElevation) },
                    singleLine = true,
                    isError = elevationError,
                    supportingText = if (elevationError) {
                        { Text(strings.invalidNumber) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = accuracy,
                    onValueChange = {
                        accuracy = it
                        accuracyError = false
                    },
                    label = { Text(strings.fieldAccuracy) },
                    singleLine = true,
                    isError = accuracyError,
                    supportingText = if (accuracyError) {
                        { Text(strings.invalidNumber) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }

            Text(
                text = strings.category,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PointCategory.entries.forEach { entry ->
                    FilterChip(
                        selected = category == entry,
                        onClick = { category = entry },
                        label = { Text(strings.forCategory(entry)) },
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings.fieldDescription) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val e = easting.replace(',', '.').toDoubleOrNull()
                    val n = northing.replace(',', '.').toDoubleOrNull()
                    val z = elevation.replace(',', '.').toDoubleOrNull()
                    val a = accuracy.replace(',', '.').toDoubleOrNull()
                    codeError = code.isBlank()
                    eastingError = e == null
                    northingError = n == null
                    elevationError = z == null
                    accuracyError = accuracy.isNotBlank() && a == null
                    if (codeError || eastingError || northingError || elevationError || accuracyError) return@Button
                    onSave(
                        PointDraft(
                            code = code.trim(),
                            easting = e ?: 0.0,
                            northing = n ?: 0.0,
                            elevation = z ?: 0.0,
                            category = category,
                            description = description.trim(),
                            accuracy = a ?: 0.0,
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(strings.savePoint)
            }
        }
    }
}

private fun formatForField(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString() else value.toString()

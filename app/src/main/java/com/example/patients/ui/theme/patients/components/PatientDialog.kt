package com.example.patients.ui.theme.patients.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.patients.R
import com.example.patients.data.models.Patient
import com.example.patients.data.models.PatientRequest

@Composable
fun PatientDialog(
    patient: Patient? = null,
    onDismiss: () -> Unit,
    onConfirm: (PatientRequest) -> Unit
) {
    var nom by rememberSaveable { mutableStateOf(patient?.nom ?: "") }
    var prenom by rememberSaveable { mutableStateOf(patient?.prenom ?: "") }
    var age by rememberSaveable { mutableStateOf(patient?.age?.toString() ?: "") }
    var tel by rememberSaveable { mutableStateOf(patient?.telephone ?: "") }
    var medecin by rememberSaveable { mutableStateOf(patient?.medecin ?: "") }

    val isValid = nom.isNotBlank() && prenom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                stringResource(
                    if (patient == null) R.string.dialog_new_title 
                    else R.string.dialog_edit_title
                )
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nom,
                    onValueChange = { nom = it },
                    label = { Text(stringResource(R.string.label_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = prenom,
                    onValueChange = { prenom = it },
                    label = { Text(stringResource(R.string.label_firstname)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.label_age)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tel,
                        onValueChange = { tel = it },
                        label = { Text(stringResource(R.string.label_phone)) },
                        modifier = Modifier.weight(2f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = medecin,
                    onValueChange = { medecin = it },
                    label = { Text(stringResource(R.string.label_doctor)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                if (!isValid && (nom.isNotEmpty() || prenom.isNotEmpty())) {
                    Text(
                        text = stringResource(R.string.error_required),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    onConfirm(
                        PatientRequest(
                            nom = nom.trim(),
                            prenom = prenom.trim(),
                            age = age.toIntOrNull(),
                            telephone = tel.trim().ifBlank { null },
                            medecin = medecin.trim().ifBlank { null }
                        )
                    )
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text(stringResource(R.string.action_cancel)) 
            }
        }
    )
}

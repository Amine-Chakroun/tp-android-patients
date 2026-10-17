package com.example.patients.ui.theme.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.patients.R
import com.example.patients.data.models.Patient
import com.example.patients.ui.theme.patients.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsScreen(
    viewModel: PatientViewModel = viewModel(factory = PatientViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedPatient by remember { mutableStateOf<Patient?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Gestion des messages (Snackbar)
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp
            ) {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.title_patients),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (uiState is PatientsUiState.Success) {
                                    val count = (uiState as PatientsUiState.Success).patients.size
                                    Text(
                                        text = stringResource(R.string.count_format, count),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    )
                    // Barre de recherche (Composant dans PatientsContent.kt)
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                    // Filtres (Composant dans PatientsContent.kt)
                    FilterRow(
                        currentFilter = filter,
                        onFilterSelected = { viewModel.setStatusFilter(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is PatientsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PatientsUiState.Error -> {
                    // Utilisation du composant ErrorState défini dans PatientsContent.kt
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.loadPatients() }
                    )
                }
                is PatientsUiState.Success -> {
                    // Filtrage local pour la recherche
                    val filteredPatients = state.patients.filter {
                        it.nom.contains(searchQuery, ignoreCase = true) ||
                                it.prenom.contains(searchQuery, ignoreCase = true)
                    }

                    // Liste principale avec Pull-to-refresh
                    PatientsContent(
                        patients = filteredPatients,
                        isRefreshing = isRefreshing,
                        onRefresh = { viewModel.refresh() },
                        onPatientClick = { selectedPatient = it },
                        onPatientDelete = { viewModel.deletePatient(it) }
                    )
                }
            }
        }
    }

    // Dialogues
    if (showAddDialog) {
        PatientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { request ->
                viewModel.addPatient(request)
                showAddDialog = false
            }
        )
    }

    selectedPatient?.let { patient ->
        PatientDialog(
            patient = patient,
            onDismiss = { selectedPatient = null },
            onConfirm = { request ->
                viewModel.updatePatient(patient.id, request)
                selectedPatient = null
            }
        )
    }
}
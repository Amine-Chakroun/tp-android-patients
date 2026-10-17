package com.example.patients.ui.theme.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.patients.PatientsApplication
import com.example.patients.data.models.Patient
import com.example.patients.data.models.PatientRepository
import com.example.patients.data.models.PatientRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PatientViewModel(private val repository: PatientRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<PatientsUiState>(PatientsUiState.Loading)
    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    // Filtre par statut (null = Tous, "Actif", "Inactif")
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // État du Pull-to-refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            // N'affiche le loader central que si ce n'est pas un refresh manuel
            if (!_isRefreshing.value) {
                _uiState.value = PatientsUiState.Loading
            }
            
            repository.getPatients(_statusFilter.value)
                .onSuccess { patients ->
                    _uiState.value = PatientsUiState.Success(patients)
                }
                .onFailure { e ->
                    _uiState.value = PatientsUiState.Error(e.message ?: "Erreur de chargement")
                }
            _isRefreshing.value = false
        }
    }

    fun refresh() {
        _isRefreshing.value = true
        loadPatients()
    }

    fun setStatusFilter(status: String?) {
        if (_statusFilter.value != status) {
            _statusFilter.value = status
            loadPatients()
        }
    }

    fun addPatient(request: PatientRequest) {
        viewModelScope.launch {
            repository.createPatient(request)
                .onSuccess { p ->
                    updateLocalList { it + p }
                    _messages.emit("${p.prenom} ${p.nom} ajouté")
                }
                .onFailure { e ->
                    _messages.emit(e.message ?: "Ajout impossible")
                }
        }
    }

    fun updatePatient(id: String, request: PatientRequest) {
        viewModelScope.launch {
            repository.updatePatient(id, request)
                .onSuccess { p ->
                    updateLocalList { list -> list.map { if (it.id == id) p else it } }
                    _messages.emit("${p.prenom} ${p.nom} modifié")
                }
                .onFailure { e ->
                    _messages.emit(e.message ?: "Mise à jour impossible")
                }
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            repository.deletePatient(patient.id)
                .onSuccess {
                    updateLocalList { list -> list.filterNot { it.id == patient.id } }
                    _messages.emit("${patient.prenom} ${patient.nom} supprimé")
                }
                .onFailure { e ->
                    _messages.emit(e.message ?: "Suppression impossible")
                }
        }
    }

    private fun updateLocalList(transform: (List<Patient>) -> List<Patient>) {
        _uiState.update { state ->
            if (state is PatientsUiState.Success) {
                state.copy(patients = transform(state.patients))
            } else {
                state
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PatientsApplication
                PatientViewModel(app.container.patientRepository)
            }
        }
    }
}

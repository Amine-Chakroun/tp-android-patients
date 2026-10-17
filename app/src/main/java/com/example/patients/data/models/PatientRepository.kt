package com.example.patients.data.models

import com.example.patients.data.models.remote.PatientApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class PatientRepository(
    private val api: PatientApi,
    private val json: Json
) {
    suspend fun getPatients(statut: String? = null): Result<List<Patient>> = safeCall { 
        api.getPatients(statut) 
    }

    suspend fun createPatient(request: PatientRequest): Result<Patient> = safeCall { 
        api.createPatient(request) 
    }

    suspend fun updatePatient(id: String, request: PatientRequest): Result<Patient> = safeCall {
        api.updatePatient(id, request)
    }

    suspend fun deletePatient(id: String): Result<Unit> = safeCall {
        val response = api.deletePatient(id)
        if (!response.isSuccessful) throw HttpException(response)
    }

    private suspend fun <T> safeCall(call: suspend () -> T): Result<T> =
        try {
            Result.success(call())
        } catch (e: HttpException) {
            Result.failure(Exception(messageFrom(e)))
        } catch (e: IOException) {
            Result.failure(Exception("Serveur injoignable. Vérifiez votre connexion."))
        } catch (e: SerializationException) {
            Result.failure(Exception("Erreur de données serveur."))
        }

    private fun messageFrom(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()
        val apiError = body?.let {
            runCatching { json.decodeFromString(ApiError.serializer(), it) }.getOrNull()
        }
        return apiError?.message ?: "Erreur ${e.code()}"
    }
}

@Serializable
data class Patient(
    val id: String,
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null,
    val statut: String = "Actif"
)

@Serializable
data class PatientRequest(
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null
)

@Serializable
data class ApiError(
    val message: String,
    val erreurs: List<String> = emptyList()
)

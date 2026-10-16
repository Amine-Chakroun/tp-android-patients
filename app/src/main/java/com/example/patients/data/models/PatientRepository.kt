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

    suspend fun getPatients(): Result<List<Patient>> = safeCall { api.getPatients() }

    private suspend fun <T> safeCall(call: suspend () -> T): Result<T> =
        try {
            Result.success(call())
        } catch (e: HttpException) {          // le serveur a répondu 4xx ou 5xx
            Result.failure(Exception(messageFrom(e)))
        } catch (e: IOException) {            // pas de réponse : serveur éteint, mauvaise IP...
            Result.failure(Exception("Serveur injoignable. Vérifiez l'adresse et le réseau."))
        } catch (e: SerializationException) { // JSON reçu différent de la classe Kotlin
            Result.failure(Exception("Réponse inattendue du serveur."))
        }

    // Lit le corps JSON de l'erreur renvoyé par notre API Node
    private fun messageFrom(e: HttpException): String {
        val body = e.response()?.errorBody()?.string()
        val apiError = body?.let {
            runCatching { json.decodeFromString(ApiError.serializer(), it) }.getOrNull()
        }
        return when {
            apiError == null -> "Erreur ${e.code()}"
            apiError.erreurs.isNotEmpty() -> apiError.erreurs.joinToString("\n")
            else -> apiError.message
        }
    }
}

// Patient tel que renvoyé par l'API (GET)
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

// Données envoyées pour créer ou modifier un patient (POST / PUT) : pas d'id
@Serializable
data class PatientRequest(
    val nom: String,
    val prenom: String,
    val age: Int? = null,
    val telephone: String? = null,
    val medecin: String? = null
)

// Corps des réponses d'erreur de l'API : { "message": "...", "erreurs": [...] }
@Serializable
data class ApiError(
    val message: String,
    val erreurs: List<String> = emptyList()
)

package com.example.patients.data.models.remote

import com.example.patients.data.models.Patient
import com.example.patients.data.models.PatientRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PatientApi {

    @GET("patients")
    suspend fun getPatients(@Query("statut") statut: String? = null): List<Patient>

    @GET("patients/{id}")
    suspend fun getPatient(@Path("id") id: String): Patient

    @POST("patients")
    suspend fun createPatient(@Body patient: PatientRequest): Patient

    @PUT("patients/{id}")
    suspend fun updatePatient(@Path("id") id: String, @Body patient: PatientRequest): Patient

    // 204 = réponse sans corps : on récupère la réponse brute
    @DELETE("patients/{id}")
    suspend fun deletePatient(@Path("id") id: String): Response<Unit>
}

package com.dug.sun.api

import com.dug.sun.model.LoginRequest
import com.dug.sun.model.LoginResponse
import com.dug.sun.model.PlanStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/api/mobile/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/mobile/logout")
    suspend fun logout(): Response<Unit>

    @GET("/api/mobile/plant-status")
    suspend fun getPlanStatus(): Response<PlanStatusResponse>
}
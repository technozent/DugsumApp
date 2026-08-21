package com.dug.sun.api

import com.dug.sun.model.ErrorResponse
import com.google.gson.Gson
import retrofit2.Response

fun <T> Response<T>.getErrorMessage(): String {
    return try {
        val errorBody = errorBody()?.string()
        if (errorBody != null) {
            val errorResponse = Gson().fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message ?: "Unknown error"
        } else {
            "An error occurred"
        }
    } catch (e: Exception) {
        "Error: ${code()}"
    }
}
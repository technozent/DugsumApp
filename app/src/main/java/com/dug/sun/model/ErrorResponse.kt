package com.dug.sun.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error") val message: String?
)
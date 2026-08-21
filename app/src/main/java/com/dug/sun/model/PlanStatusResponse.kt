package com.dug.sun.model

import com.google.gson.annotations.SerializedName

data class PlanStatusResponse(
    @SerializedName("username") val username: String?,
    @SerializedName("planStartDate") val planStartDate: String?,
    @SerializedName("planEndDate") val planEndDate: String?,
    @SerializedName("isExpired") val isExpired: Boolean
)
package com.dug.sun.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("mobile") val mobile: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceId") val deviceId: String
)

data class LoginResponse(
    @SerializedName("additionalProp1") val username: String?,
    @SerializedName("additionalProp2") val planStartDate: String?,
    @SerializedName("additionalProp3") val planEndDate: String?,
    @SerializedName("accessToken") val accessToken: String?
)
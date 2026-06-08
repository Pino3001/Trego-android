package com.grupo6.trego.data.model

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("token") val token: String
)
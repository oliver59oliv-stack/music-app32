package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RadioDto(
    val name: String,
    val url: String,
    val logo: String?
)

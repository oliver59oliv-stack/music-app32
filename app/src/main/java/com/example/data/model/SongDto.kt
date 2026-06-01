package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SongDto(
    val id: String,
    val title: String,
    val artist: String?,
    val thumb: String?,
    val url: String,
    val source: String
)

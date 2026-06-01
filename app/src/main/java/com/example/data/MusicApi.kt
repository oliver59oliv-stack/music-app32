package com.example.data

import com.example.data.model.SongDto
import retrofit2.http.GET

interface MusicApi {
    @GET("songs")
    suspend fun getSongs(): List<SongDto>
}

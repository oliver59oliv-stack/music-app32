package com.example.data

import com.example.data.model.SongDto
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object MusicRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://music-worker.ma68.workers.dev/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(MusicApi::class.java)

    suspend fun getSongs(): List<SongDto> {
        return api.getSongs()
    }
}

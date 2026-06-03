package com.example.data

import com.example.data.model.RadioDto
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RadioRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://radio-worker.ma68.workers.dev/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(RadioApi::class.java)

    suspend fun getRadios(): List<RadioDto> {
        return api.getRadios()
    }
}

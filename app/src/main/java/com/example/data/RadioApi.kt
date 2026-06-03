package com.example.data

import com.example.data.model.RadioDto
import retrofit2.http.GET

interface RadioApi {
    @GET("radios")
    suspend fun getRadios(): List<RadioDto>
}

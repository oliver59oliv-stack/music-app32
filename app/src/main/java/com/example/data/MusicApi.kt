package com.example.data

import com.example.data.model.SongDto
import com.squareup.moshi.JsonClass
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class YoutubeThumbnailDto(
    val url: String?
)

@JsonClass(generateAdapter = true)
data class YoutubeAuthorDto(
    val title: String?
)

@JsonClass(generateAdapter = true)
data class YoutubeVideoDto(
    val videoId: String?,
    val title: String?,
    val author: YoutubeAuthorDto?,
    val thumbnails: List<YoutubeThumbnailDto>?
)

@JsonClass(generateAdapter = true)
data class YoutubeContentDto(
    val type: String?,
    val video: YoutubeVideoDto?
)

@JsonClass(generateAdapter = true)
data class YoutubeSearchResponse(
    val contents: List<YoutubeContentDto>?
)

interface MusicApi {
    @GET("songs")
    suspend fun getSongs(): List<SongDto>

    @GET("search")
    suspend fun searchYoutube(@Query("q") query: String): YoutubeSearchResponse

    @GET("download")
    suspend fun downloadSong(@Query("url") url: String): ResponseBody
}

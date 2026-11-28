package com.vishnu.app

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class TmdbPage(val results: List<TmdbTitle> = emptyList())

data class TmdbTitle(
    val id: Int,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val poster_path: String? = null,
    val backdrop_path: String? = null,
    val vote_average: Double? = null,
    val media_type: String? = null
) {
    val displayTitle: String get() = title ?: name ?: "Untitled"
}

interface TmdbApi {
    @GET("api/tmdb")
    suspend fun getCatalogue(@Query("path") path: String): TmdbPage
}

object TmdbClient {
    val api: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://vishnu-two-bice.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }
}

package com.example.vpnapp

import com.example.vpnapp.models.ServerListResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ServerApi {
    @GET("servers.example.json")
    suspend fun getServers(): ServerListResponse

    companion object {
        private const val BASE_URL = "https://raw.githubusercontent.com/shahadat30339/Allnetbd/main/"

        fun create(): ServerApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ServerApi::class.java)
        }
    }
}

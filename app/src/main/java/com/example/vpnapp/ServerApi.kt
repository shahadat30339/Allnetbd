package com.example.vpnapp

import com.example.vpnapp.models.ServerListResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ServerApi {
    // Replace with the real URL where you host servers.json
    @GET("servers.json")
    suspend fun getServers(): ServerListResponse

    companion object {
        // CHANGE THIS to your own server / GitHub raw URL
        private const val BASE_URL = "https://yourdomain.com/api/"

        fun create(): ServerApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ServerApi::class.java)
        }
    }
}

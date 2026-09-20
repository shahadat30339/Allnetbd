package com.example.vpnapp

import android.content.Context
import com.example.vpnapp.models.Server
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Simple local cache using SharedPreferences so the app still has a server
// list even if the network fetch fails (e.g. no internet on first launch).
object ServerRepository {

    private const val PREFS = "vpn_prefs"
    private const val KEY_SERVERS = "cached_servers"

    private val api = ServerApi.create()

    suspend fun refreshServerList(context: Context): List<Server> {
        val response = api.getServers() // throws on failure, caller should catch
        saveServers(context, response.servers)
        return response.servers
    }

    fun saveServers(context: Context, servers: List<Server>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = Gson().toJson(servers)
        prefs.edit().putString(KEY_SERVERS, json).apply()
    }

    fun getCachedServers(context: Context): List<Server> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SERVERS, null) ?: return emptyList()
        val type = object : TypeToken<List<Server>>() {}.type
        return Gson().fromJson(json, type)
    }
}

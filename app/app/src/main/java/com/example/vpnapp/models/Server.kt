package com.example.vpnapp.models

import java.io.Serializable

// Matches the servers.json format discussed earlier.
// "type" is either "vless" (V2Ray/Xray) or "ssh".
data class Server(
    val id: String,
    val type: String,          // "vless" or "ssh"
    val country: String,
    val host: String,
    val port: Int,

    // --- vless / xray fields (nullable, only used when type == "vless") ---
    val uuid: String? = null,
    val network: String? = null,   // e.g. "ws"
    val path: String? = null,      // e.g. "/vless"
    val tls: Boolean? = null,

    // --- ssh fields (nullable, only used when type == "ssh") ---
    val username: String? = null,
    val auth: String? = null,      // "password" or "key"
    val password: String? = null
) : Serializable

data class ServerListResponse(
    val version: Int,
    val servers: List<Server>
)

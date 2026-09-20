package com.example.vpnapp

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.vpnapp.models.Server

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var connectButton: Button
    private lateinit var selectServerButton: Button

    private var selectedServer: Server? = null

    private val vpnPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startVpnService()
        } else {
            statusText.text = "VPN permission denied"
        }
    }

    private val serverPickerLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedServer = result.data?.getSerializableExtra("selected_server") as? Server
            statusText.text = "Selected: ${selectedServer?.country ?: "None"}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        connectButton = findViewById(R.id.connectButton)
        selectServerButton = findViewById(R.id.selectServerButton)

        selectServerButton.setOnClickListener {
            serverPickerLauncher.launch(Intent(this, ServerListActivity::class.java))
        }

        connectButton.setOnClickListener {
            val server = selectedServer
            if (server == null) {
                statusText.text = "Pick a server first"
                return@setOnClickListener
            }
            requestVpnPermissionAndConnect(server)
        }
    }

    private fun requestVpnPermissionAndConnect(server: Server) {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val server = selectedServer ?: return
        val intent = Intent(this, VpnConnectionService::class.java).apply {
            action = VpnConnectionService.ACTION_CONNECT
            putExtra(VpnConnectionService.EXTRA_SERVER, server)
        }
        startForegroundService(intent)
        statusText.text = "Connecting..."
    }

    fun disconnectVpn() {
        val intent = Intent(this, VpnConnectionService::class.java).apply {
            action = VpnConnectionService.ACTION_DISCONNECT
        }
        startService(intent)
        statusText.text = "Disconnected"
    }
}

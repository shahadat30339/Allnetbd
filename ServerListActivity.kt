package com.example.vpnapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.vpnapp.models.Server
import kotlinx.coroutines.launch

class ServerListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private var servers: List<Server> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_list)

        listView = findViewById(R.id.serverListView)

        // Show cached servers immediately, then try to refresh from network
        servers = ServerRepository.getCachedServers(this)
        renderList()
        refreshFromNetwork()

        listView.setOnItemClickListener { _, _, position, _ ->
            val server = servers[position]
            val resultIntent = Intent().putExtra("selected_server", server)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun refreshFromNetwork() {
        lifecycleScope.launch {
            try {
                servers = ServerRepository.refreshServerList(this@ServerListActivity)
                renderList()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ServerListActivity,
                    "Could not refresh server list (using cached copy)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun renderList() {
        val labels = servers.map { "${it.country} (${it.type.uppercase()})" }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
    }
}

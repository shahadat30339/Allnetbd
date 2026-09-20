package com.example.vpnapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.example.vpnapp.models.Server
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Skeleton VpnService.
 *
 * IMPORTANT: This is a starting point, not a finished tunnel implementation.
 * Routing raw IP packets from the TUN interface into an SSH SOCKS5 proxy or
 * into Xray requires a userspace TCP/IP stack (e.g. tun2socks) sitting
 * between the TUN file descriptor and the proxy. That piece is non-trivial
 * and is usually reused from existing open-source projects rather than
 * written from scratch:
 *   - For SSH: pair this with a tun2socks binary + JSch's SOCKS5 forwarding
 *   - For Xray/V2Ray: use AndroidLibXrayLite, which already includes this
 *     packet routing logic
 */
class VpnConnectionService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var sshSession: Session? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        const val ACTION_CONNECT = "com.example.vpnapp.CONNECT"
        const val ACTION_DISCONNECT = "com.example.vpnapp.DISCONNECT"
        const val EXTRA_SERVER = "extra_server"
        const val CHANNEL_ID = "vpn_service_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val server = intent.getSerializableExtra(EXTRA_SERVER) as? Server
                if (server != null) startVpn(server)
            }
            ACTION_DISCONNECT -> stopVpn()
        }
        return START_STICKY
    }

    private fun startVpn(server: Server) {
        startForeground(NOTIFICATION_ID, buildNotification("Connecting to ${server.country}..."))

        // 1. Establish the TUN interface (captures all device traffic)
        val builder = Builder()
            .setSession("MyVPN")
            .addAddress("10.0.0.2", 24)
            .addDnsServer("1.1.1.1")
            .addRoute("0.0.0.0", 0)

        vpnInterface = builder.establish()

        // 2. Depending on server type, start the right backend
        when (server.type) {
            "ssh" -> connectViaSsh(server)
            "vless" -> connectViaXray(server)
        }
    }

    private fun connectViaSsh(server: Server) {
        scope.launch {
            try {
                val jsch = JSch()
                val session = jsch.getSession(server.username, server.host, server.port)
                session.setPassword(server.password)
                session.setConfig("StrictHostKeyChecking", "no")
                session.connect(15000)

                // Dynamic port forwarding = local SOCKS5 proxy on 127.0.0.1:1080
                session.setPortForwardingL(1080, "", 0) // real signature: setPortForwardingD in JSch
                sshSession = session

                // TODO: route packets from vpnInterface (TUN fd) into this
                // SOCKS5 proxy using a tun2socks-style component.
                updateNotification("Connected via SSH (${server.host})")
            } catch (e: Exception) {
                updateNotification("Failed to connect: ${e.message}")
                stopVpn()
            }
        }
    }

    private fun connectViaXray(server: Server) {
        // TODO: integrate AndroidLibXrayLite here.
        // Typical flow:
        //   1. Build an Xray JSON config in memory using server.uuid/host/port/path
        //   2. Start the Xray core (it opens a local SOCKS5/HTTP proxy)
        //   3. Route TUN traffic into that local proxy (tun2socks)
        updateNotification("Xray connection not yet implemented")
    }

    private fun stopVpn() {
        sshSession?.disconnect()
        sshSession = null
        vpnInterface?.close()
        vpnInterface = null
        stopForeground(true)
        stopSelf()
    }

    private fun buildNotification(text: String): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, "VPN Status", NotificationManager.IMPORTANCE_LOW)
        manager.createNotificationChannel(channel)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("VPN")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(text))
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}

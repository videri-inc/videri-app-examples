package com.videri.testapp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.videri.lib.icanvasplayer.VServiceBroadcastDefinitions
import com.videri.lib.icanvasplayer.VServiceConnection
import com.videri.lib.icanvasplayer.VServiceConnection.ConnectionListener
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private val tag = "ie:MainActivity"
    private val commandMap = HashMap<String, String>()

    private var textView: TextView? = null

    private val pendingCommands = ArrayList<String>()

    private val connectionListener: ConnectionListener = object : ConnectionListener {
        @SuppressLint("StaticFieldLeak")
        override fun onConnected() {
            try {
                updateText("icanvasplayer version: " + vServiceConnection.version)
                updateText("icanvasplayer serial: " + vServiceConnection.uid)
                val allowedCommands = vServiceConnection.getAllowedCommands(applicationContext)
                if (allowedCommands != null) {
                    updateText("# commands allowed: " + allowedCommands.size)
                }
            } catch (e: RemoteException) {
                updateText(e.toString())
            }

            sendPendingCommands()
        }

        override fun onDisconnected() {
            updateText("Disconnected from vservice")
        }
    }

    private val vServiceConnection = VServiceConnection(connectionListener)

    private fun updateText(s: String) {
        Log.i(tag, s)
        this.textView?.append("\n" + s)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.status_text)

        updateText("icanvasplayer Build.getSerial(): " + this.serial)

        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val uuid = intent.getStringExtra(VServiceBroadcastDefinitions.EXTRA_UUID)
                val response = intent.getStringExtra(VServiceBroadcastDefinitions.EXTRA_MESSAGE)
                val success = intent.getBooleanExtra(VServiceBroadcastDefinitions.EXTRA_SUCCESS, false)
                val command = commandMap[uuid]
                val text =  "Result of command: '$command' -> ${if (success) "Success" else "Error"}\nResponse: '$response'"
                updateText(text)
            }
        }
        registerResponseReceiver(receiver)
    }

    @get:SuppressLint("HardwareIds")
    private val serial: String?
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Build.getSerial()
            } else {
                @Suppress("DEPRECATION")
                return Build.SERIAL
            }
        }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun registerResponseReceiver(receiver: BroadcastReceiver?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, IntentFilter(VServiceBroadcastDefinitions.ACTION_DEMO_COMMAND_RESPONSE), RECEIVER_EXPORTED)
        } else {
            registerReceiver(receiver, IntentFilter(VServiceBroadcastDefinitions.ACTION_DEMO_COMMAND_RESPONSE))
        }
    }

    override fun onStart() {
        super.onStart()
        // Get the current status
        sendDemoCommand("get_connection_state:=true")
        sendDemoCommand("get_settings_json:=|[\"available_timezones\",\"available_hdmi_resolutions\"]")
        sendDemoCommand("get_part_info")
        sendDemoCommand("get_provisioning_info_json")
        sendDemoCommand("get_status")
        sendDemoCommand("get_manufacturing_info:=true")
        val isBinding = vServiceConnection.connect(this)
        updateText("isBinding: $isBinding")
        vServiceConnection.setConnectionListener(connectionListener)
    }

    override fun onStop() {
        super.onStop()
        textView?.text = ""
        vServiceConnection.disconnect()
        synchronized(this) {
            pendingCommands.clear()
        }
    }

    @Synchronized
    private fun sendDemoCommand(command: String) {
        // We can only send commands if the service is bound - so we will attempt to send it now and retry after binding if needed
        this.pendingCommands.add(command)
        sendPendingCommands()
    }

    @Synchronized
    private fun sendPendingCommands() {
        if (vServiceConnection.isBound) {
            for (command in pendingCommands) {
                Log.i(tag, "Sending command $command")
                val uuid = UUID.randomUUID().toString()
                try {
                    val status = vServiceConnection.sendCommand(this.applicationContext, command, uuid)
                    if (status == VServiceConnection.CmdStatus.SUCCESSFULLY_SENT) {
                        // Add the message id to this map so that we can match the responses later
                        commandMap[uuid] = command
                    } else {
                        updateText("Failed to send command: $command with status: $status")
                    }
                } catch (e: Exception) {
                    updateText("Error sending command $command: $e")
                }
            }
            pendingCommands.clear()
        }
    }
}

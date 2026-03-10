package com.videri.testapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.videri.lib.icanvasplayer.VServiceBroadcastDefinitions
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class CommandReceiver : BroadcastReceiver() {

    private val tag = "ie:CommandReceiver"

    private val cmdExecutor: Executor = Executors.newCachedThreadPool()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        when (action) {
            VServiceBroadcastDefinitions.BROADCAST_ACTION_RECIEVE_MESSAGE -> runCommandAsync(context, intent)
        }
    }

    /**
     * This method spawns a new threads to handle the commands received, so as not to block the UI thread.
     * Note that you still only have less than 10 seconds to work with.  If you want to do more, start a
     * Service or IntentService to handle the command.
     * 
     * @param context - context to send response with
     * @param intent - intent of the command
     */
    private fun runCommandAsync(context: Context, intent: Intent) {
        this.cmdExecutor.execute { runCommand(intent, context) }
    }

    /**
     * Actually handle the command here
     * 
     * @param intent - intent that command came from
     * @param context - context to use for reply
     */
    private fun runCommand(intent: Intent, context: Context) {
        val command = intent.getStringExtra(VServiceBroadcastDefinitions.BROADCAST_EXTRA_MSG_COMMAND)
        val params = intent.getStringArrayExtra(VServiceBroadcastDefinitions.BROADCAST_EXTRA_MSG_PARAMS)
        Log.i(tag, "received command: $command")
        var message: String?
        var isSuccessful = true
        // All commands need to be registered as metadata in the manifest
        try {
            when (command) {
                "com.videri.testapp:help" -> {
                    message = "com.videri.testapp:help - this help text"
                    message += "\ncom.videri.testapp:info - simple example command"
                    message += "\ncom.videri.testapp:echo - usage: com.videri.testapp:echo:={text to echo}"
                }

                "com.videri.testapp:info" -> message = "This app is an example of how to interface with the icanvasplayer"
                "com.videri.testapp:echo" -> message = String.format(Locale.US, "Command: %s, Params: %s", command, params.contentToString())

                else -> {
                    // this could only happen if a command is registered in the manifest, but not here
                    message = "$command command not handled"
                    isSuccessful = false
                }
            }
        } catch (e: Exception) {
            message = e.toString()
            isSuccessful = false
        }
        sendResponse(context, intent, message, isSuccessful)
    }

    /**
     * Sends a response to a command
     * @param context - context to use to send the broadcast
     * @param originalIntent - the original intent that contained the command
     * @param message - message to reply with
     * @param success - if succeeded or failed
     */
    private fun sendResponse(context: Context, originalIntent: Intent?, message: String?, success: Boolean) {
        val intent = VServiceBroadcastDefinitions.getDemoResponseIntent(originalIntent, message, success)
        context.sendBroadcast(intent)
    }
}

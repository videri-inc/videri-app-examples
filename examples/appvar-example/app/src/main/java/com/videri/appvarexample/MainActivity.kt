package com.videri.appvarexample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var varsTextView: TextView
    private lateinit var lifecycleStatusTextView: TextView

    /**
     * LifecycleEventObserver is more reliable in determining that the Activity is in the background / foreground because:
     * - an Activity with a transparent background may be shown on top of this Activity, causing the onStop() method to not be called
     * - this Activity will be called multiple times with the FLAG_ACTIVITY_SINGLE_TOP flag, causing onPause() + onResume() to be called multiple times while in the foreground
     */
    private val lifecycleEventObserver: LifecycleEventObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.i(TAG, "OnLifecycleEvent - resume() - onMoveToForeground() Returning to foreground…")
                    onAppForegrounded()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    Log.i(TAG, "OnLifecycleEvent - pause() - onMoveToBackground() Moving to background…")
                    onAppBackgrounded()
                }

                else -> {
                    Log.i(TAG, "OnLifecycleEvent - $event")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
        varsTextView = findViewById<TextView>(R.id.vars_text)
        lifecycleStatusTextView = findViewById<TextView>(R.id.lifecycle_status_text)
    }

    /**
     * This activity will be launched with the following flags:
     * Intent.FLAG_ACTIVITY_SINGLE_TOP
     * Intent.FLAG_ACTIVITY_NEW_TASK
     * Intent.FLAG_ACTIVITY_NO_ANIMATION
     *
     * This means every subsequent launch will be received here, so if the variables are changed, this is where it can be detected
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // So that the intent is available in onResume
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val intent = getIntent()
        val sb = StringBuilder("Vars:")
        if (intent != null) {
            val extras = intent.getExtras()
            if (extras != null) {
                for (key in extras.keySet()) {
                    val value = "" + extras.get(key)
                    val line = String.format(Locale.US, "\n%s: %s", key, value)
                    sb.append(line)
                }
            }
        }
        varsTextView.text = sb.toString()
    }

    private fun onAppBackgrounded() {
        lifecycleStatusTextView.text = "Status: In Background"
        //TODO - stuff you should do when the app goes in the background
    }

    private fun onAppForegrounded() {
        lifecycleStatusTextView.text = "Status: In Foreground"
        //TODO - stuff you should do when the app returns to the foreground
    }

    companion object {
        private const val TAG = "AppVarExample"
    }
}

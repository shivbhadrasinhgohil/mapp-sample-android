package com.example.mappsample

import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Random

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        val label = TextView(this)
        label.text = "MAPP sample app: insecure on purpose, demo material for a scanner."
        root.addView(label)
        setContentView(root)

        // MAPP demo: DEX-WEAK-CIPHER, DEX-STATIC-IV-OR-KEY (see InsecureCrypto)
        InsecureCrypto.encrypt("hello")
        // MAPP demo: DEX-WEAK-HASH (see InsecureCrypto)
        InsecureCrypto.hash("hello")

        // MAPP demo: DEX-SHAREDPREFS-SENSITIVE, DEX-EXTERNAL-STORAGE-WRITE (see InsecureStorage)
        InsecureStorage.savePassword(this, "hunter2")

        // MAPP demo: DEX-LOGGING-PRESENT (logs a string that mentions a password)
        Log.d("MainActivity", "password=" + "hunter2")

        // MAPP demo: DEX-INSECURE-RANDOM (java.util.Random in a method named for a token)
        Log.d("MainActivity", "session=" + newSessionToken())

        // MAPP demo: DEX-RUNTIME-EXEC (command built from an intent extra)
        try {
            Runtime.getRuntime().exec("ping -c 1 " + intent.getStringExtra("host"))
        } catch (e: Exception) {
            Log.w("MainActivity", "exec failed")
        }

        // MAPP demo: DEX-WORLD-ACCESSIBLE-FILES (MODE_WORLD_READABLE throws on API 24+, never run)
        readLegacyPrefs()
    }

    override fun onResume() {
        super.onResume()
        // MAPP demo: DEX-DYNAMIC-RECEIVER-UNPROTECTED (no permission, no RECEIVER_NOT_EXPORTED)
        try {
            registerReceiver(DemoReceiver(), IntentFilter("com.example.mappsample.PING"))
        } catch (e: Exception) {
            Log.w("MainActivity", "registerReceiver failed")
        }
    }

    // Named for a token on purpose: DEX-INSECURE-RANDOM looks at the method name.
    private fun newSessionToken(): String {
        val random = Random()
        return "sess-" + random.nextInt()
    }

    @Suppress("DEPRECATION")
    private fun readLegacyPrefs() {
        try {
            getSharedPreferences("legacy", Context.MODE_WORLD_READABLE)
        } catch (e: Exception) {
            Log.w("MainActivity", "legacy prefs unavailable")
        }
    }
}

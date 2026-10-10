package com.example.mappsample

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dalvik.system.DexClassLoader
import java.net.URL

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
// The service is exported without any permission in AndroidManifest.xml
// (MANIFEST-EXPORTED-SERVICE).
class SyncService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val dexPath = intent?.getStringExtra("dex")
        if (dexPath != null) {
            try {
                // MAPP demo: DEX-DYNAMIC-CODE-LOADING (loads code from a path chosen by the caller)
                DexClassLoader(dexPath, codeCacheDir.absolutePath, null, classLoader)
            } catch (e: Exception) {
                Log.w("SyncService", "dex load failed")
            }
        }

        object : Thread() {
            override fun run() {
                try {
                    // MAPP demo: DEX-HTTP-URL-LITERAL (cleartext http:// endpoint)
                    val connection = URL("http://demo.example.com/api").openConnection()
                    connection.connect()
                } catch (e: Exception) {
                    Log.w("SyncService", "sync failed")
                }
            }
        }.start()

        return START_NOT_STICKY
    }
}

package com.example.mappsample

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
// The receiver is exported without any permission in AndroidManifest.xml
// (MANIFEST-EXPORTED-RECEIVER).
class DemoReceiver : BroadcastReceiver() {

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }
        // MAPP demo: DEX-INTENT-REDIRECT (starts an Intent supplied by the sender)
        val target = intent.getParcelableExtra<Intent>("target") ?: return
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(target)
    }
}

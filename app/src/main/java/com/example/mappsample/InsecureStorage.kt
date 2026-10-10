package com.example.mappsample

import android.content.Context
import java.io.File

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
object InsecureStorage {

    fun savePassword(ctx: Context, pw: String) {
        // MAPP demo: DEX-SHAREDPREFS-SENSITIVE (plain-text password in SharedPreferences)
        ctx.getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("password", pw)
            .apply()

        // MAPP demo: DEX-EXTERNAL-STORAGE-WRITE (password written to external app storage)
        File(ctx.getExternalFilesDir(null), "notes.txt").writeText(pw)
    }
}

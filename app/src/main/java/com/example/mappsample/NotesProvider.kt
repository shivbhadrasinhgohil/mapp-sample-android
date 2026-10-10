package com.example.mappsample

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
// The provider is exported without any permission in AndroidManifest.xml
// (MANIFEST-EXPORTED-PROVIDER) and builds SQL from the caller's selection.
class NotesProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val ctx = context ?: return null
        // MAPP demo: DEX-SQL-RAW-QUERY (caller-controlled selection concatenated into SQL)
        return NotesDb(ctx).readableDatabase.rawQuery(
            "SELECT * FROM notes WHERE title = '" + selection + "'",
            null
        )
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }
}

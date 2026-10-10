package com.example.mappsample

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// MAPP SAMPLE APP: INTENTIONALLY INSECURE demo material for the MAPP Scan scanner.
// Never ship this code. Each "MAPP demo:" comment names the rule it is meant to trigger.
class NotesDb(context: Context) : SQLiteOpenHelper(context, "notes.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE notes (id INTEGER PRIMARY KEY, title TEXT, body TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // no-op
    }

    fun findByTitle(title: String): Cursor {
        // MAPP demo: DEX-SQL-RAW-QUERY (SQL built by string concatenation)
        return readableDatabase.rawQuery("SELECT * FROM notes WHERE title = '" + title + "'", null)
    }
}

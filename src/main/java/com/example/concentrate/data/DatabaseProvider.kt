package com.example.concentrate.data

import android.content.Context
import kotlinx.serialization.json.Json

object DatabaseProvider {
    private var database: OnuDatabase? = null

    fun getDatabase(context: Context): OnuDatabase {
        if (database == null) {
            val jsonString = context.assets.open("onu_programs_database.json").bufferedReader().use { it.readText() }
            database = Json { ignoreUnknownKeys = true }.decodeFromString(jsonString)
        }
        return database!!
    }
}

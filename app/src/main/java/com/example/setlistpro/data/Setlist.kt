package com.example.setlistpro.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "setlists")
data class Setlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val pdfUris: List<Uri> // Room needs help storing this list
)

// Helper to convert List<Uri> to String and back
class Converters {
    @TypeConverter
    fun fromUriList(uris: List<Uri>): String {
        return uris.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toUriList(data: String): List<Uri> {
        if (data.isEmpty()) return emptyList()
        return data.split(",").map { Uri.parse(it) }
    }
}

package com.example.setlistpro.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Insert
    suspend fun insertSetlist(setlist: Setlist)

    @Query("SELECT * FROM setlists ORDER BY id DESC")
    fun getAllSetlists(): Flow<List<Setlist>>
}

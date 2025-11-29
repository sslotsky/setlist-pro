package com.example.setlistpro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {
    @Insert
    suspend fun insertSetlist(setlist: Setlist)

    @Update
    suspend fun updateSetlist(setlist: Setlist)

    @Delete
    fun deleteSetlist(setlist: Setlist)

    @Query("SELECT * FROM setlists ORDER BY id DESC")
    fun getAllSetlists(): Flow<List<Setlist>>

    @Query("SELECT * FROM setlists WHERE id = :id")
    fun getSetlistById(id: Int): Flow<Setlist>
}

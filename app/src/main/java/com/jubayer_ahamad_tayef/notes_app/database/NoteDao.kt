package com.jubayer_ahamad_tayef.notes_app.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jubayer_ahamad_tayef.notes_app.model.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM NOTES ORDER BY noteNumber")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM NOTES WHERE noteNumber LIKE :query OR id LIKE :query OR noteTime LIKE :query OR noteDate LIKE :query OR noteTitle LIKE :query OR noteDescription LIKE :query")
    fun searchNote(query: String?): LiveData<List<Note>>

    @Query("SELECT COUNT(*) FROM notes WHERE number = :noteNumber")
    suspend fun isNumberAlreadyExists(noteNumber: Long): Int
}
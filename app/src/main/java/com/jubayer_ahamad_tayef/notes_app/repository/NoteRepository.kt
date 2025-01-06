package com.jubayer_ahamad_tayef.notes_app.repository

import com.jubayer_ahamad_tayef.notes_app.database.NoteDatabase
import com.jubayer_ahamad_tayef.notes_app.model.Note

class NoteRepository(private val database: NoteDatabase) {

    suspend fun insertNote(note: Note) = database.getNoteDao().insertNote(note)
    suspend fun updateNote(note: Note) = database.getNoteDao().updateNote(note)
    suspend fun deleteNote(note: Note) = database.getNoteDao().deleteNote(note)

    fun getAllNotes() = database.getNoteDao().getAllNotes()
    fun searchNote(query: String?) = database.getNoteDao().searchNote(query)

    suspend fun doesNumberAlreadyExist(noteNumber: Long): Boolean {
        val count = database.getNoteDao().isNumberAlreadyExists(noteNumber)
        return count > 0
    }
}
package com.jubayer_ahamad_tayef.notes_app.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jubayer_ahamad_tayef.notes_app.repository.NoteRepository

class NoteViewModelFactory(val app: Application, private val noteRepository: NoteRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NoteViewModel(app, noteRepository) as T
    }
}
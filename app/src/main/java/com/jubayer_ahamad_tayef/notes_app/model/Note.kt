package com.jubayer_ahamad_tayef.notes_app.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "notes")
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val noteNumber: String,
    val noteTitle: String,
    val noteDescription: String,
    val noteDate: String?,
    val noteTime: String?
) : Parcelable

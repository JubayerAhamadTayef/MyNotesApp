package com.jubayer_ahamad_tayef.notes_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jubayer_ahamad_tayef.notes_app.databinding.NoteItemDesignBinding
import com.jubayer_ahamad_tayef.notes_app.fragments.HomeFragmentDirections
import com.jubayer_ahamad_tayef.notes_app.model.Note

class NoteAdapter : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    class NoteViewHolder(val itemBinding: NoteItemDesignBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id && oldItem.noteTitle == newItem.noteTitle && oldItem.noteDescription == newItem.noteDescription && oldItem.noteDate == newItem.noteDate && oldItem.noteTime == newItem.noteTime
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            NoteItemDesignBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = differ.currentList[position]

        holder.itemBinding.time.text = currentNote.noteTime
        holder.itemBinding.date.text = currentNote.noteDate
        holder.itemBinding.noteNo.text = currentNote.noteNumber
        holder.itemBinding.noteTitle.text = currentNote.noteTitle
        holder.itemBinding.noteDescription.text = currentNote.noteDescription

        holder.itemView.setOnClickListener {

            it.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(currentNote))
        }

    }
}
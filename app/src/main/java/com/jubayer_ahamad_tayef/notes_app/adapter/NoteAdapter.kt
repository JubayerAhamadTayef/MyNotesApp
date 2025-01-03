package com.jubayer_ahamad_tayef.notes_app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jubayer_ahamad_tayef.notes_app.databinding.NoteItemDesignBinding
import com.jubayer_ahamad_tayef.notes_app.fragments.HomeFragmentDirections
import com.jubayer_ahamad_tayef.notes_app.model.Note

class NoteAdapter(var context: Context) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    private var lastPosition = -1;

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
        holder.itemBinding.noteNo.text = currentNote.noteNumber.toString()
        holder.itemBinding.noteTitle.text = currentNote.noteTitle
        holder.itemBinding.noteDescription.text = currentNote.noteDescription
        setAnimation(holder.itemView, position)

        if (currentNote.noteDate?.isBlank() == true){
            holder.itemBinding.dateIcon.visibility = View.GONE
        }
        if (currentNote.noteTime?.isBlank() == true){
            holder.itemBinding.timeIcon.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {

            it.findNavController()
                .navigate(HomeFragmentDirections.actionHomeFragmentToEditNoteFragment(currentNote))
        }

    }

    private fun setAnimation(viewToAnimate: View, position: Int) {

        if (position > lastPosition) {
            val animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left)
            viewToAnimate.startAnimation(animation)
            lastPosition = position
        }
    }

}
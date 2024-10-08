package com.jubayer_ahamad_tayef.notes_app.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.jubayer_ahamad_tayef.notes_app.MainActivity
import com.jubayer_ahamad_tayef.notes_app.R
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentEditNoteBinding
import com.jubayer_ahamad_tayef.notes_app.model.Note
import com.jubayer_ahamad_tayef.notes_app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat

class EditNoteFragment : Fragment(R.layout.fragment_edit_note), MenuProvider {

    private var editNoteBinding: FragmentEditNoteBinding? = null
    private val binding get() = editNoteBinding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var currentNote: Note

    private val calendar: Calendar by lazy { Calendar.getInstance() }
    private lateinit var showDate: String
    private lateinit var showTime: String

    private val args: EditNoteFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        editNoteBinding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.setTitle("Edit Or Delete Note")

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        currentNote = args.note!!

        binding.apply {

            editNoteNoEditText.setText(currentNote.noteNumber.toString())
            editNoteTitleEditText.setText(currentNote.noteTitle)
            editNoteDescriptionEditText.setText(currentNote.noteDescription)
            pickADate.setText(currentNote.noteDate)
            pickATime.setText(currentNote.noteTime)

            pickADate.setOnClickListener {

                pickADate()

            }
            pickATime.setOnClickListener {

                pickATime()

            }

        }

    }

    private fun deleteNote() {
        AlertDialog.Builder(activity, R.style.CustomDatePickerDialog).apply {
            setTitle("Delete Note")
            setMessage("Do you want to delete this Note?")
            setPositiveButton("Yes") { _, _ ->
                notesViewModel.deleteNote(currentNote)
                Toast.makeText(context, "Note Deleted!", Toast.LENGTH_SHORT).show()

                view?.findNavController()?.popBackStack(R.id.homeFragment, false)
            }
            setNegativeButton("No", null)
        }.create().show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.edit_note_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.deleteMenu -> {
                deleteNote()
                true
            }

            else -> false

        } || when (menuItem.itemId) {
            R.id.updateMenu -> {
                updateNote()
                true
            }

            else -> false
        }
    }

    private fun updateNote() {
        binding.apply {
            val noteNumber = editNoteNoEditText.text.toString().trim()
            val noteTitle = editNoteTitleEditText.text.toString().trim()
            val noteDescription = editNoteDescriptionEditText.text.toString().trim()
            val noteDate = pickADate.text.toString().trim()
            val noteTime = pickATime.text.toString().trim()

            if (noteNumber.isNotEmpty() && noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {

                val note = Note(
                    currentNote.id,
                    noteNumber,
                    noteTitle,
                    noteDescription,
                    noteDate,
                    noteTime
                )
                notesViewModel.updateNote(note)
                Toast.makeText(context, "Note Updated!", Toast.LENGTH_SHORT).show()

                view?.findNavController()?.popBackStack(R.id.homeFragment, false)

            } else {

                Toast.makeText(
                    context,
                    "Please, Must be enter Note Number, Note Title and also Note Description. Then click again Update Note!",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    private fun pickADate() {

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        DatePickerDialog(
            requireActivity(),
            R.style.CustomDatePickerDialog,
            DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                showDate = "$day/${month + 1}/$year"
                binding.pickADate.setText(showDate)
            },
            year,
            month,
            day
        ).show()

    }

    @SuppressLint("SimpleDateFormat")
    private fun pickATime() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireActivity(),
            R.style.CustomTimePickerDialog,
            TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)

                val timeFormatAmPm = SimpleDateFormat("hh:mm aa")
                val timeFormat24Hour = SimpleDateFormat("HH:mm")

                showTime = when (timePicker.is24HourView) {
                    true -> timeFormat24Hour.format(calendar.time)
                    false -> timeFormatAmPm.format(calendar.time)
                }

                binding.pickATime.setText(showTime)

            },
            hour,
            minute,
            false
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        editNoteBinding = null
    }
}


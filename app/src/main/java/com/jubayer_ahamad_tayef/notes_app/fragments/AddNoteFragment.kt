package com.jubayer_ahamad_tayef.notes_app.fragments

import android.annotation.SuppressLint
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
import com.jubayer_ahamad_tayef.notes_app.MainActivity
import com.jubayer_ahamad_tayef.notes_app.R
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentAddNoteBinding
import com.jubayer_ahamad_tayef.notes_app.model.Note
import com.jubayer_ahamad_tayef.notes_app.viewmodel.NoteViewModel
import java.text.SimpleDateFormat

class AddNoteFragment : Fragment(R.layout.fragment_add_note), MenuProvider {

    private var addNoteBinding: FragmentAddNoteBinding? = null
    private val binding get() = addNoteBinding!!

    private val calendar: Calendar by lazy { Calendar.getInstance() }
    private lateinit var showDate: String
    private lateinit var showTime: String

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var addNoteView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        addNoteBinding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.setTitle("Add Note")

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        addNoteView = view

        binding.apply {

            pickADate.setOnClickListener {

                pickADate()

            }
            pickATime.setOnClickListener {

                pickATime()

            }

        }

    }

    private fun saveNote(view: View) {
        val noteNumber = binding.addNoteNoEditText.text.toString().trim()
        val noteTitle = binding.addNoteTitleEditText.text.toString().trim()
        val noteDescription = binding.addNoteDescriptionEditText.text.toString().trim()
        val noteDate = binding.pickADate.text.toString().trim()
        val noteTime = binding.pickATime.text.toString().trim()

        if (noteNumber.isNotEmpty() && noteTitle.isNotEmpty() && noteDescription.isNotEmpty() && noteDate.isNotEmpty() && noteTime.isNotEmpty()) {

            val note = Note(0, noteNumber, noteTitle, noteDescription, noteDate, noteTime)
            notesViewModel.addNote(note)

            Toast.makeText(addNoteView.context, "Note Saved", Toast.LENGTH_SHORT).show()

            view.findNavController().popBackStack(R.id.homeFragment, false)
        } else {

            Toast.makeText(
                addNoteView.context,
                "Please, Enter all needed information. Then click again save!",
                Toast.LENGTH_SHORT
            ).show()

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

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.add_note_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.saveMenu -> {
                saveNote(addNoteView)
                true
            }

            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addNoteBinding = null
    }

}
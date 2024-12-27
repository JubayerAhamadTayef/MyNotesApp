package com.jubayer_ahamad_tayef.notes_app.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

        noteNumberFocusChangeListener()
        noteTitleFocusChangeListener()
        noteDescriptionFocusChangeListener()

        (activity as MainActivity).supportActionBar?.title = getString(R.string.edit_or_delete_note)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        currentNote = args.note!!

        binding.apply {

            editNoteNoEditText.setText(currentNote.noteNumber)
            editNoteTitleEditText.setText(currentNote.noteTitle)
            editNoteDescriptionEditText.setText(currentNote.noteDescription)
            pickADate.text = currentNote.noteDate
            pickATime.text = currentNote.noteTime

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
            setTitle(getString(R.string.delete_note))
            setMessage(getString(R.string.do_you_want_to_delete_this_note))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                notesViewModel.deleteNote(currentNote)
                showToast(getString(R.string.note_deleted))

                view?.findNavController()?.popBackStack(R.id.homeFragment, false)
            }
            setNegativeButton(getString(R.string.no), null)
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

            editNoteNoEditTextLayout.helperText = isNoteNumberValid()
            editNoteTitleEditTextLayout.helperText = isNoteTitleValid()
            editNoteDescriptionEditTextLayout.helperText = isNoteDescriptionValid()

            if (noteNumber.isNotEmpty() && noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {

                val note = Note(
                    currentNote.id, noteNumber, noteTitle, noteDescription, noteDate, noteTime
                )
                notesViewModel.updateNote(note)
                showToast(getString(R.string.note_updated))

                view?.findNavController()?.popBackStack(R.id.homeFragment, false)

            } else {

                showToast(getString(R.string.please_must_be_enter_note_number_note_title_and_also_note_description_then_click_again_update_note))

            }
        }
    }

    private fun pickADate() {

        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        DatePickerDialog(
            requireActivity(),
            R.style.CustomDatePickerDialog,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                showDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.pickADate.text = showDate
            },
            currentYear,
            currentMonth,
            currentDay
        ).show()

    }

    @SuppressLint("SimpleDateFormat")
    private fun pickATime() {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireActivity(),
            R.style.CustomTimePickerDialog,
            TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)

                val timeFormatAmPm = SimpleDateFormat(getString(R.string.hh_mm_aa))
                val timeFormat24Hour = SimpleDateFormat(getString(R.string.hh_mm))

                showTime = when (timePicker.is24HourView) {
                    true -> timeFormat24Hour.format(calendar.time)
                    false -> timeFormatAmPm.format(calendar.time)
                }

                binding.pickATime.text = showTime

            },
            currentHour,
            currentMinute,
            false
        ).show()
    }

    private fun noteNumberFocusChangeListener() {
        binding.editNoteNoEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.editNoteNoEditTextLayout.helperText = isNoteNumberValid()
            }
            noteNumberTextChangeListener()
        }
    }

    private fun noteNumberTextChangeListener() {
        binding.editNoteNoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.editNoteNoEditTextLayout.helperText = isNoteNumberValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isNoteNumberValid(): CharSequence? {
        val noteNumber = binding.editNoteNoEditText.text.toString().trim()
        return when {
            noteNumber.isBlank() -> getString(R.string.note_number_is_required)
            noteNumber.isNotBlank() -> null
            else -> getString(R.string.invalid_note_number)
        }
    }

    private fun noteTitleFocusChangeListener() {
        binding.editNoteTitleEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.editNoteTitleEditTextLayout.helperText = isNoteTitleValid()
            }
            noteTitleTextChangeListener()
        }
    }

    private fun noteTitleTextChangeListener() {
        binding.editNoteTitleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.editNoteTitleEditTextLayout.helperText = isNoteTitleValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isNoteTitleValid(): CharSequence? {
        val noteTitle = binding.editNoteTitleEditText.text.toString().trim()
        return when {
            noteTitle.isBlank() -> getString(R.string.note_title_is_required)
            noteTitle.isNotBlank() -> null
            else -> getString(R.string.invalid_note_title)
        }
    }

    private fun noteDescriptionFocusChangeListener() {
        binding.editNoteDescriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.editNoteDescriptionEditTextLayout.helperText = isNoteDescriptionValid()
            }
            noteDescriptionTextChangeListener()
        }
    }

    private fun noteDescriptionTextChangeListener() {
        binding.editNoteDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.editNoteDescriptionEditTextLayout.helperText = isNoteDescriptionValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isNoteDescriptionValid(): CharSequence? {
        val noteDescription = binding.editNoteDescriptionEditText.text.toString().trim()
        return when {
            noteDescription.isBlank() -> getString(R.string.note_description_is_required)
            noteDescription.isNotBlank() -> null
            else -> getString(R.string.invalid_note_description)
        }
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editNoteBinding = null
    }
}


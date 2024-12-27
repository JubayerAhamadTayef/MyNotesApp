package com.jubayer_ahamad_tayef.notes_app.fragments

import android.annotation.SuppressLint
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

        noteNumberFocusChangeListener()
        noteTitleFocusChangeListener()
        noteDescriptionFocusChangeListener()

        (activity as MainActivity).supportActionBar?.title = getString(R.string.add_note)

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

        binding.addNoteNoEditTextLayout.helperText = isNoteNumberValid()
        binding.addNoteTitleEditTextLayout.helperText = isNoteTitleValid()
        binding.addNoteDescriptionEditTextLayout.helperText = isNoteDescriptionValid()

        if (noteNumber.isNotEmpty() && noteTitle.isNotEmpty() && noteDescription.isNotEmpty()) {

            val note = Note(0, noteNumber, noteTitle, noteDescription, noteDate, noteTime)
            notesViewModel.addNote(note)

            showToast(getString(R.string.note_saved))

            view.findNavController().popBackStack(R.id.homeFragment, false)
        } else {

           showToast(getString(R.string.please_must_be_enter_note_number_note_title_and_also_note_description_then_click_again_update_note))

        }
    }

    private fun pickADate() {

        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        DatePickerDialog(
            requireActivity(),
            R.style.CustomDatePickerDialog,
            DatePickerDialog.OnDateSetListener { datePicker, selectedYear, selectedMonth, selectedDay ->
                showDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.pickADate.text = showDate
            },
            year,
            month,
            day
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
        binding.addNoteNoEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.addNoteNoEditTextLayout.helperText = isNoteNumberValid()
            }
            noteNumberTextChangeListener()
        }
    }

    private fun noteNumberTextChangeListener() {
        binding.addNoteNoEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.addNoteNoEditTextLayout.helperText = isNoteNumberValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isNoteNumberValid(): CharSequence? {
        val noteNumber = binding.addNoteNoEditText.text.toString().trim()
        return when {
            noteNumber.isBlank() -> getString(R.string.note_number_is_required)
            noteNumber.isNotBlank() -> null
            else -> getString(R.string.invalid_note_number)
        }
    }

    private fun noteTitleFocusChangeListener() {
        binding.addNoteTitleEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.addNoteTitleEditTextLayout.helperText = isNoteTitleValid()
            }
            noteTitleTextChangeListener()
        }
    }

    private fun noteTitleTextChangeListener() {
        binding.addNoteTitleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.addNoteTitleEditTextLayout.helperText = isNoteTitleValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isNoteTitleValid(): CharSequence? {
        val noteTitle = binding.addNoteTitleEditText.text.toString().trim()
        return when {
            noteTitle.isBlank() -> getString(R.string.note_title_is_required)
            noteTitle.isNotBlank() -> null
            else -> getString(R.string.invalid_note_title)
        }
    }

    private fun noteDescriptionFocusChangeListener() {
        binding.addNoteDescriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.addNoteDescriptionEditTextLayout.helperText = isNoteDescriptionValid()
            }
            noteDescriptionTextChangeListener()
        }
    }

    private fun noteDescriptionTextChangeListener() {
        binding.addNoteDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.addNoteDescriptionEditTextLayout.helperText = isNoteDescriptionValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isNoteDescriptionValid(): CharSequence? {
        val noteDescription = binding.addNoteDescriptionEditText.text.toString().trim()
        return when {
            noteDescription.isBlank() -> getString(R.string.note_description_is_required)
            noteDescription.isNotBlank() -> null
            else -> getString(R.string.invalid_note_description)
        }
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

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addNoteBinding = null
    }

}
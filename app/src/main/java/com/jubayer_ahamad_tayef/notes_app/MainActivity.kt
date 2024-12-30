package com.jubayer_ahamad_tayef.notes_app

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.jubayer_ahamad_tayef.notes_app.database.NoteDatabase
import com.jubayer_ahamad_tayef.notes_app.databinding.ActivityMainBinding
import com.jubayer_ahamad_tayef.notes_app.fragments.HomeFragment
import com.jubayer_ahamad_tayef.notes_app.repository.NoteRepository
import com.jubayer_ahamad_tayef.notes_app.viewmodel.NoteViewModel
import com.jubayer_ahamad_tayef.notes_app.viewmodel.NoteViewModelFactory

class MainActivity : AppCompatActivity() {

    lateinit var noteViewModel: NoteViewModel
    private var mainBinding: ActivityMainBinding? = null
    private val binding get() = mainBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        supportActionBar?.setTitle("My Notes")

        setUpViewModel()
    }

    private fun setUpViewModel() {

        val noteRepository = NoteRepository(NoteDatabase(this))
        val viewModelProviderFactory = NoteViewModelFactory(application, noteRepository)
        noteViewModel = ViewModelProvider(this, viewModelProviderFactory)[NoteViewModel::class.java]

    }

    override fun onBackPressed() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        val currentFragment = navHostFragment.childFragmentManager.fragments[0]

        if (currentFragment is HomeFragment) {

            AlertDialog.Builder(this, R.style.CustomDatePickerDialog).apply {
                setTitle("Exit")
                setMessage("Do you want to Exit?")
                setPositiveButton("Yes") { _, _ ->
                    finish()
                }
                setNegativeButton("No", null)
            }.create().show()

        } else {
            super.onBackPressed()
        }
    }

    fun showActionBar(show: Boolean) {
        if (show) supportActionBar?.show() else supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainBinding = null
    }

}
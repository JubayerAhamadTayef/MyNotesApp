package com.jubayer_ahamad_tayef.notes_app.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.jubayer_ahamad_tayef.notes_app.MainActivity
import com.jubayer_ahamad_tayef.notes_app.R
import com.jubayer_ahamad_tayef.notes_app.adapter.NoteAdapter
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentHomeBinding
import com.jubayer_ahamad_tayef.notes_app.model.Note
import com.jubayer_ahamad_tayef.notes_app.viewmodel.NoteViewModel

class HomeFragment : Fragment(R.layout.fragment_home), SearchView.OnQueryTextListener,
    MenuProvider {

    private var homeBinding: FragmentHomeBinding? = null
    private val binding get() = homeBinding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        (activity as MainActivity).showActionBar(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.setTitle("My Notes")

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        setupHomeRecyclerView()

        binding.addNoteButton.setOnClickListener {
            it.findNavController().navigate(R.id.action_homeFragment_to_addNoteFragment)
        }

    }

    private fun updateUI(note: List<Note>?) {

        if (note != null) {

            if (note.isNotEmpty()) {

                binding.apply {

                    emptyNoteImage.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                }

            } else {

                binding.apply {

                    recyclerView.visibility = View.GONE
                    emptyNoteImage.visibility = View.VISIBLE

                }

            }

        }

    }

    private fun setupHomeRecyclerView() {
        noteAdapter = NoteAdapter(requireContext())
        binding.recyclerView.apply {

            setHasFixedSize(true)
            adapter = noteAdapter
        }
        activity?.let {

            notesViewModel.getAllNote().observe(viewLifecycleOwner) { note ->
                noteAdapter.differ.submitList(note)
                updateUI(note)
            }

        }
    }

    private fun searchNote(query: String?) {

        val searchQuery = "%$query%"

        notesViewModel.searchNote(searchQuery).observe(this) { list ->
            noteAdapter.differ.submitList(list)
        }

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {

            searchNote(newText)

        }
        return true
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)

        val menuSearch = menu.findItem(R.id.searchMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)

    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.logOut -> {
                logOut()
                true
            }

            else -> false
        }
    }

    private fun logOut() {
        AlertDialog.Builder(activity, R.style.CustomDatePickerDialog).apply {
            setTitle("Logout")
            setMessage("Do you want to Logout?")
            setPositiveButton("Yes") { _, _ ->
                val auth = FirebaseAuth.getInstance()

                auth.signOut().apply {

                    Toast.makeText(context, "Logout Successfully!", Toast.LENGTH_SHORT).show()

                    view?.findNavController()?.popBackStack(R.id.welcomeFragment, false)

                }
            }
            setNegativeButton("No", null)
        }.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        homeBinding = null
    }

}
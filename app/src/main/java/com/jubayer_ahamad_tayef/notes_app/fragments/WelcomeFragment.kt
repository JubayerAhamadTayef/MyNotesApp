package com.jubayer_ahamad_tayef.notes_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jubayer_ahamad_tayef.notes_app.MainActivity
import com.jubayer_ahamad_tayef.notes_app.R
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var welcomeBinding: FragmentWelcomeBinding? = null
    private val binding get() = welcomeBinding!!

    private lateinit var firebaseUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        welcomeBinding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.setTitle("Welcome")

        FirebaseAuth.getInstance().currentUser?.let {

            firebaseUser = it
            findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)

        }
        binding.apply {

            loginButton.setOnClickListener {

                findNavController().navigate(R.id.action_welcomeFragment_to_loginFragment)
            }

            registerButton.setOnClickListener {

                findNavController().navigate(R.id.action_welcomeFragment_to_registerFragment)
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        welcomeBinding = null
    }

}
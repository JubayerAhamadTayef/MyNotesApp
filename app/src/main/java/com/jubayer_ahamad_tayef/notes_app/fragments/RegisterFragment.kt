package com.jubayer_ahamad_tayef.notes_app.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.jubayer_ahamad_tayef.notes_app.MainActivity
import com.jubayer_ahamad_tayef.notes_app.R
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var registerBinding: FragmentRegisterBinding? = null
    private val binding get() = registerBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        registerBinding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.setTitle("Register Please")

        binding.apply {

            alreadyHaveAccount.setOnClickListener {

                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)

            }


            registerButton.setOnClickListener {

                if (userEditText.text?.isNotEmpty() == true && emailEditText.text?.isNotEmpty() == true && passwordEditText.text?.isNotEmpty() == true) {

                    val userName = userEditText.text.toString().trim()
                    val email = emailEditText.text.toString().trim()
                    val password = passwordEditText.text.toString().trim()

                    if (isEmailValid(email) && isPasswordValid(password)) {

                        signUpUser(userName, email, password)

                    } else {

                        Toast.makeText(
                            requireContext(),
                            "Invalid Email or Password!",
                            Toast.LENGTH_SHORT
                        ).show()

                    }


                } else {

                    Toast.makeText(
                        requireContext(),
                        "Please, provide needed Information, then click again Register Button.",
                        Toast.LENGTH_SHORT
                    ).show()

                }

            }

        }

    }
    private fun isEmailValid(email: String): Boolean {

        return Patterns.EMAIL_ADDRESS.matcher(email).matches()

    }

    private fun isPasswordValid(password: String): Boolean {

        val passRegex = Regex("^(?=.*[A-Za-z])(?=.*[@\$!%*#?&])[A-Za-z@\$!%*#?&\\d]{6,}\$")
        return password.matches(passRegex)

    }

    private fun signUpUser(userName: String, email: String, password: String) {

        val auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {

                Toast.makeText(requireContext(), "Register Successfully, $email", Toast.LENGTH_SHORT)
                    .show()

                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)

            } else {

                Toast.makeText(requireContext(), "${it.exception?.message}", Toast.LENGTH_SHORT)
                    .show()

            }

        }

    }
    override fun onDestroy() {
        super.onDestroy()
        registerBinding = null
    }

}
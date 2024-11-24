package com.jubayer_ahamad_tayef.notes_app.fragments

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.jubayer_ahamad_tayef.notes_app.MainActivity
import com.jubayer_ahamad_tayef.notes_app.R
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RegisterFragment : Fragment() {

    private var registerBinding: FragmentRegisterBinding? = null
    private val binding get() = registerBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        registerBinding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userNameFocusChangeListener()
        userEmailFocusChangeListener()
        userPasswordFocusChangeListener()

        (activity as MainActivity).supportActionBar?.title = getString(R.string.register_please)

        binding.apply {
            alreadyHaveAccount.setOnClickListener {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
            registerButton.setOnClickListener {
                validateForm()
            }
        }
    }

    private fun validateForm() {
        val isUserNameValid = isUserNameValid()
        val isEmailValid = isEmailValid()
        val isPasswordStrong = isPasswordStrong()

        if (isUserNameValid == null && isEmailValid == null && isPasswordStrong == null) {
            lifecycleScope.launch {
                if (isConnected(requireContext())) {
                    signUpUser(
                        binding.userNameEditText.text.toString().trim(),
                        binding.emailEditText.text.toString().trim(),
                        binding.passwordEditText.text.toString().trim()
                    )
                } else {
                    showToast(getString(R.string.no_internet_connection_please_connect_to_internet))
                }
            }
        } else {
            showFormErrors(isUserNameValid, isEmailValid, isPasswordStrong)
        }
    }

    private fun showFormErrors(
        userNameError: CharSequence?, emailError: CharSequence?, passwordError: CharSequence?
    ) {
        binding.apply {
            userNameEditTextLayout.error = userNameError
            emailEditTextLayout.error = emailError
            passwordEditTextLayout.helperText = passwordError
            showToast(getString(R.string.please_provide_valid_information_and_try_again))
        }
    }

    private fun userPasswordFocusChangeListener() {
        binding.passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.passwordEditTextLayout.helperText = isPasswordStrong()
            }
            userPasswordTextChangedListener()
        }
    }

    private fun userPasswordTextChangedListener() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.passwordEditTextLayout.helperText = isPasswordStrong()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun isPasswordStrong(): String? {
        val password = binding.passwordEditText.text.toString().trim()
        return when {
            password.isBlank() -> getString(R.string.password_required)
            password.length < 6 -> getString(R.string.password_length)
            !password.matches(".*[A-Z].*".toRegex()) -> getString(R.string.password_uppercase)
            !password.matches(".*[a-z].*".toRegex()) -> getString(R.string.password_lowercase)
            !password.matches(".*[0-9].*".toRegex()) -> getString(R.string.password_number)
            !password.matches(".*[@#\$%^&+=].*".toRegex()) -> getString(R.string.password_special_character)
            else -> null
        }
    }

    private fun userEmailFocusChangeListener() {
        binding.emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.emailEditTextLayout.error = isEmailValid()
            }
            userEmailTextChangeListener()
        }
    }

    private fun userEmailTextChangeListener() {
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailEditTextLayout.error = isEmailValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isEmailValid(): CharSequence? {
        val email = binding.emailEditText.text.toString().trim()
        return when {
            email.isBlank() -> getString(R.string.email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() || !Regex(".*\\.[a-z]{3,}$").matches(
                email
            ) -> getString(R.string.enter_valid_email)

            else -> null
        }
    }

    private fun userNameFocusChangeListener() {
        binding.userNameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.userNameEditTextLayout.error = isUserNameValid()
            }
            userNameTextChangeListener()
        }
    }

    private fun userNameTextChangeListener() {
        binding.userNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.userNameEditTextLayout.error = isUserNameValid()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun isUserNameValid(): CharSequence? {
        val userName = binding.userNameEditText.text.toString().trim()
        return when {
            userName.isBlank() -> getString(R.string.user_name_is_required)
            userName.matches(".*[a-z].*".toRegex()) || userName.matches(".*[A-Z].*".toRegex()) -> null
            else -> getString(R.string.user_name_must_contain_at_least_one_uppercase_or_lowercase_letter)
        }
    }

    private fun signUpUser(userName: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val user = createUserWithEmailAndPassword(email, password)
                if (user != null) {
                    sendVerificationEmail(user)
                    showAlertDialog(
                        getString(R.string.register_success),
                        getString(R.string.verify_email_message, email)
                    )
                    findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                } else {
                    showToast(getString(R.string.registration_failed))
                }
            } catch (e: Exception) {
                showToast(e.message ?: getString(R.string.unknown_error))
            }
        }
    }

    private suspend fun createUserWithEmailAndPassword(
        email: String, password: String
    ): FirebaseUser? {
        return suspendCoroutine { continuation ->
            val auth = FirebaseAuth.getInstance()
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(auth.currentUser)
                } else {
                    continuation.resumeWithException(
                        task.exception ?: Exception(getString(R.string.unknown_error))
                    )
                }
            }
        }
    }

    private fun sendVerificationEmail(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast(getString(R.string.verification_email_sent, user.email))
            } else {
                showToast(getString(R.string.failed_to_send_verification))
            }
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme).setTitle(title)
            .setMessage(message).setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            )
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        registerBinding = null
    }
}
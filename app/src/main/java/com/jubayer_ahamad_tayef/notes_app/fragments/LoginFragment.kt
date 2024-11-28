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
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LoginFragment : Fragment() {

    private var loginBinding: FragmentLoginBinding? = null
    private val binding get() = loginBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        loginBinding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userEmailFocusChangeListener()
        userPasswordFocusChangeListener()

        (activity as MainActivity).supportActionBar?.title = getString(R.string.login_please)

        binding.apply {
            createNewAccount.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }

            loginButton.setOnClickListener {
                validateForm()
            }

            forgotPassword.setOnClickListener {
                val email = emailEditText.text.toString().trim()
                if (isEmailValid() == null) {
                    sendPasswordResetEmail(email)
                } else {
                    showToast(getString(R.string.enter_valid_email))
                }
            }
        }
    }

    private fun validateForm() {
        val emailError = isEmailValid()
        val passwordError = isPasswordStrong()

        if (emailError == null && passwordError == null) {
            loginUser(
                binding.emailEditText.text.toString().trim(),
                binding.passwordEditText.text.toString().trim()
            )
        } else {
            showFormErrors(emailError, passwordError)
        }
    }

    private fun showFormErrors(emailError: CharSequence?, passwordError: CharSequence?) {
        binding.apply {
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
        }
        userPasswordTextChangedListener()
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

    private fun isPasswordStrong(): CharSequence? {
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
        }
        userEmailTextChangeListener()
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

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {
            if (isConnected(requireContext())) {
                showProgressBar(true)
                try {
                    val user = signInWithEmailAndPassword(email, password)
                    if (user != null && user.isEmailVerified) {
                        showToast(getString(R.string.login_successfully, user.email))
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    } else if (user != null) {
                        showResendVerificationDialog(user)
                    } else {
                        showToast(getString(R.string.authentication_failed))
                    }
                } catch (e: Exception) {
                    showToast(e.message ?: getString(R.string.unknown_error))
                } finally {
                    showProgressBar(false)
                }
            } else {
                showToast(getString(R.string.no_internet_connection_please_connect_to_internet))
            }
        }
    }

    private suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        return suspendCoroutine { continuation ->
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
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

    private fun showProgressBar(show: Boolean) {
        binding.loading.visibility = if (show) View.VISIBLE else View.GONE
        binding.loginPage.visibility = if (show) View.GONE else View.VISIBLE
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

    private fun sendPasswordResetEmail(email: String) {
        val auth = FirebaseAuth.getInstance()
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast(getString(R.string.password_reset_email_sent, email))
            } else {
                showToast(task.exception?.message ?: getString(R.string.failed_to_send_reset_email))
            }
        }
    }

    private fun showResendVerificationDialog(user: FirebaseUser) {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle(getString(R.string.email_not_verified))
            .setMessage(getString(R.string.resend_verification_email_message, user.email))
            .setPositiveButton(getString(R.string.resend)) { dialog, _ ->
                sendVerificationEmail(user)
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
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

    private fun showToast(message: String) {
        if (isAdded && context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loginBinding = null
    }
}

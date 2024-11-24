package com.jubayer_ahamad_tayef.notes_app.fragments

import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
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
import com.jubayer_ahamad_tayef.notes_app.databinding.FragmentWelcomeBinding
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WelcomeFragment : Fragment() {

    private var welcomeBinding: FragmentWelcomeBinding? = null
    private val binding get() = welcomeBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        welcomeBinding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).supportActionBar?.title = getString(R.string.welcome)

        lifecycleScope.launch {
            if (isConnected(requireContext())) {
                checkUserStatus()
            } else {
                noInternet()
            }
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

    private fun noInternet() {
        showToast(getString(R.string.no_internet_connection_please_connect_to_internet))
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

    private suspend fun checkUserStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val isEmailVerified = isEmailVerified(currentUser)
            if (isEmailVerified) {
                findNavController().navigate(R.id.action_welcomeFragment_to_homeFragment)
            } else {
                showVerificationAlertDialog()
            }
        }
    }

    private suspend fun isEmailVerified(user: FirebaseUser): Boolean {
        return suspendCoroutine { continuation ->
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    continuation.resume(user.isEmailVerified)
                } else {
                    continuation.resumeWithException(
                        task.exception ?: Exception(getString(R.string.unknown_error))
                    )
                }
            }
        }
    }

    private fun showVerificationAlertDialog() {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle(getString(R.string.email_not_verified))
            .setMessage(getString(R.string.verify_email_to_proceed))
            .setPositiveButton(getString(R.string.resend_verification_email)) { dialog, _ ->
                sendVerificationEmail()
                dialog.dismiss()
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
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
        welcomeBinding = null
    }
}
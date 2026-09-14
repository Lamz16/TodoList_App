package com.lamz.todolistapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.lamz.todolistapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toSignIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.signGoogle.setOnClickListener { signInWithGoogle() }
        registerAuth()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) reload()
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        resultLauncher.launch(googleSignInClient.signInIntent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let(::firebaseAuthWithGoogle) ?: showToast("Google ID token is unavailable")
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed. statusCode=${e.statusCode}", e)
            showToast("Google Sign-In failed (${e.statusCode})")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) updateUI(auth.currentUser)
            else showToast(task.exception?.message ?: "Google authentication failed")
        }
    }

    private fun registerAuth() {
        binding.btnRegis.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showToast("You must fill name, email and password")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser ?: return@addOnCompleteListener
                    val updateProfile = userProfileChangeRequest { displayName = name }
                    user.updateProfile(updateProfile).addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                delay(300L)
                                updateUI(user)
                            }
                        } else showToast(profileTask.exception?.message ?: "Failed to update profile")
                    }
                } else {
                    when (val error = task.exception) {
                        is FirebaseAuthUserCollisionException -> showToast("Users with the same email are already registered")
                        is FirebaseAuthWeakPasswordException -> showToast("Password must be more than 6 characters")
                        is FirebaseException -> showToast(error.message ?: "Registration failed")
                        else -> showToast(error?.message ?: "Registration failed")
                    }
                }
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            showToast("Welcome, ${user.displayName}")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun reload() {
        auth.currentUser?.reload()?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) updateUI(auth.currentUser)
            else showToast(task.exception?.message ?: "Unable to reload account")
        }
    }

    private fun showToast(message: String) = Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()

    companion object { const val TAG = "RegisterActivity" }
}
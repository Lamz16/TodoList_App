package com.lamz.todolistapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private val mPaint = Paint()
    var auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        mBitmap = Bitmap.createBitmap(screenWidth, 500, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)

        val colorTeks = ContextCompat.getColor(this, R.color.white)
        with(binding) {

            leStart.setTextColor(colorTeks)
            orSign.setTextColor(colorTeks)
            canvas.setImageBitmap(mBitmap)
            toSignIn.setTextColor(colorTeks)

            btnRegis.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            toSignIn.setOnClickListener {
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

            signGoogle.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    signInWithGoogle()
                }
            }
        }
        drawRectangle()
        registerAuth()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun drawRectangle() {
        val color = ContextCompat.getColor(this, R.color.color_3)
        mPaint.color = color
        mPaint.style = Paint.Style.FILL

        val left = 0f
        val top = 350F
        val right = mBitmap.width.toFloat()
        val bottom = mBitmap.height.toFloat()

        mCanvas.drawRect(left, top, right, bottom, mPaint)

    }

    private fun signInWithGoogle() {

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {

                Log.w(LoginActivity.TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    updateUI(user)
                } else {

                    updateUI(null)
                }
            }
    }

    private fun registerAuth() {
        with(binding) {
            val name = nameInput.text
            val email = emailInput.text
            val password = passwordInput.text

            btnRegis.setOnClickListener {
                if (email.toString().trim().isNotEmpty() && password.toString().trim()
                        .isNotEmpty()
                ) {

                    lifecycleScope.launch(Dispatchers.IO) {
                        auth.createUserWithEmailAndPassword(
                            email.toString(),
                            password.toString()
                        )
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    val updateProfile = userProfileChangeRequest {
                                        displayName = name.toString()
                                    }
                                    val timeDelay = 300L
                                    user!!.updateProfile(updateProfile)
                                        .addOnCompleteListener { profileTask ->
                                            if (profileTask.isSuccessful) {
                                                Toast.makeText(
                                                    baseContext,
                                                    "Welcome ${name.toString()}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                lifecycleScope.launch(Dispatchers.Main) {
                                                    delay(timeDelay)
                                                    updateUI(user)
                                                }
                                            }
                                        }
                                } else {
                                    try {
                                        throw task.exception!!
                                    } catch (e: FirebaseAuthUserCollisionException) {
                                        showToast("Users with the same email are already registered")
                                    } catch (e: FirebaseAuthWeakPasswordException) {
                                        showToast("Password must be more than 6 characters")
                                    } catch (e: FirebaseException) {
                                        showToast("Incorrect email and password")
                                    } catch (e: Exception) {
                                        showToast("${e.message}")
                                    }
                                }
                            }

                    }

                } else {
                    showToast("You must fill name, email and password")
                }
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            showToast("Welcome back, ${user.displayName}")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun reload() {
        lifecycleScope.launch(Dispatchers.IO) {
            auth.currentUser?.reload()?.addOnCompleteListener(this@RegisterActivity) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateUI(user)
                    }
                } else {
                    showToast("${task.exception}")
                }
            }
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "RegisterActivity"
    }
}
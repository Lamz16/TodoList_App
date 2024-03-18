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
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.lamz.todolistapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var mBitmap: Bitmap
    private lateinit var mCanvas: Canvas
    private val mPaint = Paint()

    private var auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        mBitmap = Bitmap.createBitmap(screenWidth, 500, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)

        val colorTeks = ContextCompat.getColor(this, R.color.white)
        with(binding) {

            sayWelcome.setTextColor(colorTeks)
            orSign.setTextColor(colorTeks)
            canvas.setImageBitmap(mBitmap)
            toSignUp.setTextColor(colorTeks)


            toSignUp.setOnClickListener {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            }
            signGoogle.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    signInWithGoogle()
                }

            }

        }

        loginAuth()

        drawRectangle()

    }

    override fun onStart() {
        super.onStart()
        auth.currentUser

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


    private fun loginAuth() {
        binding.apply{
            val email = emailInput.text
            val password = passwordInput.text

            btnLogin.setOnClickListener {

                if (email.toString().trim().isNotEmpty() && password.toString().trim()
                        .isNotEmpty()
                ) {
                    Log.d(TAG, "Inputan email: $email")
                    Log.d(TAG, "Inputan password: $password")

                    lifecycleScope.launch(Dispatchers.IO) {
                        auth.signInWithEmailAndPassword(email.toString(), password.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    val timeDelay = 300L
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        delay(timeDelay)
                                        updateUI(user)
                                    }
                                } else {
                                    try {
                                        throw task.exception!!
                                    }catch (e : FirebaseException){
                                        showToast("Incorrect email and password")
                                    }catch (e : Exception){
                                        showToast("${e.message}")
                                    }
                                }
                            }
                    }
                } else {
                   showToast("You must fill email and password")
                }
            }
        }
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
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    lifecycleScope.launch(Dispatchers.Main) {
                        updateUI(user)
                    }
                } else {
                    updateUI(null)
                }
            }
    }


    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(
                baseContext,
                "Welcome back, ${user.displayName}",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
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

    private fun showToast(message: String) {
        Toast.makeText(baseContext, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "LoginActivity"
    }

}
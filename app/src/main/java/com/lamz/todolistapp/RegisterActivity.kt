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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.lamz.todolistapp.databinding.ActivityRegisterBinding

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

        val colorBack = ContextCompat.getColor(this, R.color.color_2)
        val colorTeks = ContextCompat.getColor(this, R.color.white)
        with(binding){
            container.setBackgroundColor(colorBack)
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
                signInWithGoogle()
            }
        }
        drawRectangle()
        registerAuth()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null){
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
                // Google Sign In failed, update UI appropriately
                Log.w(LoginActivity.TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(LoginActivity.TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(LoginActivity.TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun registerAuth(){
        with(binding){
            val name = nameInput.text
            val email = emailInput.text
            val password = passwordInput.text

            btnRegis.setOnClickListener {
                if (email.toString().trim().isNotEmpty() && password.toString().trim().isNotEmpty()){
                    auth.createUserWithEmailAndPassword(email.toString() , password.toString())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                val user = auth.currentUser
                                val updateProfile = userProfileChangeRequest {
                                    displayName = name.toString()
                                }
                                user!!.updateProfile(updateProfile)
                                    .addOnCompleteListener{profileTask ->
                                    if(profileTask.isSuccessful){
                                        Toast.makeText(baseContext, "Welcome ${name.toString()}", Toast.LENGTH_SHORT).show()
                                        updateUI(user)
                                    }
                                }
                            }else{
                                Log.d("Register", "createUserWithEmail:failure", task.exception)
                                Toast.makeText(
                                    baseContext,
                                    "${task.exception}",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                }else{
                    Toast.makeText(baseContext, "You must fill name, email and password", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(user : FirebaseUser?){
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

    private fun reload() {
        auth.currentUser?.reload()?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Reload berhasil
                val user = auth.currentUser
                updateUI(user)
            } else {
                Toast.makeText(
                    baseContext,
                    "${task.exception}",
                    Toast.LENGTH_SHORT
                ).show()
                updateUI(null)
            }
        }
    }

    companion object {
        const val TAG = "RegisterActivity"
    }
}
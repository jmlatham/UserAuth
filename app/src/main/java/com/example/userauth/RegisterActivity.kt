package com.example.userauth

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var user: FirebaseUser
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var btnRegister: Button
    lateinit var errMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edtEmail = findViewById<EditText>(R.id.editEmail)
        edtPassword = findViewById<EditText>(R.id.editPassword)
        btnRegister = findViewById<Button>(R.id.btnSignUp)
        errMsg = findViewById<TextView>(R.id.regErrMsg)

        mAuth = Firebase.auth
        if(mAuth.currentUser != null) {
            user = mAuth.currentUser!!
        }

        if(intent.getStringExtra("message") != null){
            errMsg.text = intent.getStringExtra("message")
        }
        if(intent.getStringExtra("email") != null){
            edtEmail.text.replace(0, edtEmail.text.length, intent.getStringExtra("email"))
        }

        btnRegister.setOnClickListener {
            var email = edtEmail.text.toString()
            var password = edtPassword.text.toString()
            errMsg.text = ""

            if(emailIsValid(email) && passwordIsValid(password)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "createUserWithEmail:success")
                            val user = mAuth.currentUser
                            navigateToMainActivity(user)
                        } else {
                            // If registration fails, go to sign up.
                            navigateToLoginActivity(email, password)
                        }
                    }
            } else {
                Log.w(TAG, "createUserWithEmail:failure")
                Toast.makeText(
                    baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }

    private fun emailIsValid(email: String): Boolean {
        return true
    }

    private fun passwordIsValid(password: String): Boolean {
        return password.length >= 6
    }

    public override fun onStart() {
        super.onStart()
        if(mAuth.currentUser != null) {
            user = mAuth.currentUser!!
            updateUI(user)
            navigateToMainActivity(user)
        }
    }

    private fun navigateToMainActivity(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("email", user.email)
                putExtra("displayName", user.displayName)
                putExtra("phoneNumber", user.phoneNumber)
                putExtra("photoUrl", user.photoUrl)
                putExtra("user", user.toString())
            }
            startActivity(intent)
            finish()
        }
    }

    private fun navigateToLoginActivity(email: String, password: String) {
        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("email", email)
            putExtra("password", password)
        }
        startActivity(intent)
            finish()
    }

    private fun updateUI(user: Any?): Any {
        Toast.makeText(baseContext, "Authentication Successful!",
            Toast.LENGTH_SHORT).show()
        return ""
    }
}
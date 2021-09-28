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

    lateinit var myAuth: FirebaseAuth
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var btnLogin: Button
    lateinit var errMsg: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edtEmail = findViewById(R.id.editEmail)
        edtPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.btnSignUp)
        errMsg = findViewById(R.id.regErrMsg)
        myAuth = Firebase.auth

        if(intent.getStringExtra("message") != null){
            displayErrorMessage(intent.getStringExtra("message")!!)
        }

        if(intent.getStringExtra("email") != null){
            edtEmail.text.replace(0, edtEmail.text.length, intent.getStringExtra("email"))
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()

            displayErrorMessage("")
            if(emailIsValid(email) && passwordIsValid(password)) {
                loginWithUserNameAndPassword(email, password)
            } else {
                updateUI("Either email or password is invalid.")
                displayErrorMessage("Invalid Email or Password. Please try again.")
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        if(myAuth.currentUser != null) {
            updateUI("Authentication Successful!")
            navigateToMainActivity(myAuth.currentUser)
        }
    }

    private fun loginWithUserNameAndPassword(email: String, password: String) {
        myAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){
                task ->
            if (task.isSuccessful) {
                logError("createUserWithEmail:success")
                navigateToMainActivity(myAuth.currentUser)
            } else {
                createLoginWithUserAndPassword(email, password)
            }
        }
    }

    private fun createLoginWithUserAndPassword(email: String, password: String) {
        myAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    logError("createUserWithEmail:success")
                    updateUI("User created successfully!")
                    navigateToMainActivity(myAuth.currentUser)
                } else {
                    logError("createUserWithEmail:failure")
                    updateUI("Login Failed: try again")
                    displayErrorMessage("Either your password is wrong or you are trying to create a new user with an email that already exists in the system.")
                }
            }
    }

    private fun navigateToMainActivity(user: FirebaseUser?) {
        displayErrorMessage("")
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

    private fun emailIsValid(email: String): Boolean {
        // TODO: added any validity checks for the email address
        return true
    }

    private fun passwordIsValid(password: String): Boolean {
        return password.length >= 6
    }

    private fun logError(message: String){
        Log.w(TAG, message)
    }

    private fun updateUI(message: String){
        Toast.makeText(baseContext, message,
            Toast.LENGTH_SHORT).show()
    }

    private fun displayErrorMessage(message: String) {
        errMsg.text = message
    }
}
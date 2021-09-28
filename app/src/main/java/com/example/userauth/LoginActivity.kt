package com.example.userauth

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
//        displayInformationFromIntent()
        loginWithUserNameAndPassword()
    }

    private fun loginWithUserNameAndPassword() {
        val email=intent.getStringExtra("email")
        val password=intent.getStringExtra("password")
        val myAuth = Firebase.auth
        myAuth.signInWithEmailAndPassword(email!!, password!!).addOnCompleteListener(this){
                task ->
            if (task.isSuccessful) {
                Log.d(ContentValues.TAG, "createUserWithEmail:success")
                val user = myAuth.currentUser
                navigateToMainActivity(user)
            } else {
                navigateToRegisterActivity("Failed to Login to app. Please try again.")
            }
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

    private fun navigateToRegisterActivity(message: String) {
        val intent = Intent(this, RegisterActivity::class.java).apply {
            putExtra("message", message)
            putExtra("email", intent.getStringExtra("email"))
        }
        startActivity(intent)
            finish()
    }

    private fun displayInformationFromIntent(){
        val layout = findViewById<LinearLayout>(R.id.loginLinearLayout)
        val email=intent.getStringExtra("email")
        val password=intent.getStringExtra("password")
        addTextViewWithMessage(email, layout)
        addTextViewWithMessage(password, layout)
    }

    private fun addTextViewWithMessage(message: String?, layout: LinearLayout?){
        val textView = TextView(this)
        //setting height and width
        textView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        textView.text = "value: $message"
        // Add TextView to LinearLayout
        layout?.addView(textView)
    }
}
package com.example.userauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myAuth = Firebase.auth
        val user = myAuth.currentUser
        if (user != null) {
            val helloMessage = findViewById<TextView>(R.id.helloMessage)
            helloMessage.text = "Hello, ${user.email}"
        }

        val logOutButton = findViewById<Button>(R.id.btnLogout)
        logOutButton.setOnClickListener {
            myAuth.signOut()
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }
}
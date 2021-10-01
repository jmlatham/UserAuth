package com.example.userauth

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    lateinit var myAuth: FirebaseAuth
    lateinit var edtEmail: EditText
    lateinit var edtPassword: EditText
    lateinit var btnLogin: Button
    lateinit var errMsg: TextView
    private val PICK_IMAGE_REQUEST = 888
    lateinit var filePath: Uri
    lateinit var myStorage: FirebaseStorage
    lateinit var myStorageRef: StorageReference
    lateinit var myDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        edtEmail = findViewById(R.id.editEmail)
        edtPassword = findViewById(R.id.editPassword)
        btnLogin = findViewById(R.id.btnSignUp)
        errMsg = findViewById(R.id.regErrMsg)
        myAuth = Firebase.auth
//        var mStorageRef = storage.reference
        myStorage = Firebase.storage
        myStorageRef = myStorage.reference
        myDatabase = Firebase.database

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

        val imgProfile = findViewById<CircleImageView>(R.id.imgProfile)
        imgProfile.setOnClickListener{
            showImagePicker()
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
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
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
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data

//            doSomeOperations()
        }
    }

    private fun showImagePicker(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE, )
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
//        resultLauncher.launch(intent)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        TODO("refactor to use un-deprecated code")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        System.out.println("User Profile is selected")
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null){
            filePath = data.getData()!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath)
            } catch(e: IOException){
                e.printStackTrace()
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
            uploadImg()
        }
    }

    private fun uploadImg() {
//        TODO("Use Firebase Storage to upload the file")
        if (filePath != null){
            val myRef = myStorageRef.child("image/" + filePath.getLastPathSegment())
            myRef.putFile(filePath)
                .addOnSuccessListener { taskSnapshot ->
                    val result = taskSnapshot.storage.downloadUrl
                    result.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        Toast.makeText(this, "uploaded: %s".format(imageUrl), Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener{ exception ->
                        Toast.makeText(this, "downloadUrl failed: s%".format(exception.toString()), Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "putFile failed: s%".format(exception.toString()), Toast.LENGTH_SHORT).show()
                }
//                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot>(){
//                    override onSuccess(UploadTask.TaskSnapshot tasksnapshot) {
//                        Task<Uri> result = tasksnapshot.getStorage().getDownloadUrl()
//                        result.addOnSuccessListener(OnSuccessListener<Uri>(){
//                            onSuccess(Uri uri){
//                                val imageUrl = uri.toString()
//                                Toast.makeText(getApplicationContext(), "uploaded", Toast.LENGTH_SHORT).show()
//                            }
//                        })
//                    }
//                })
        }
    }
}
package com.example.mygramapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.mygramapp.databinding.ActivitySignUpBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fullnameSignup.setHint("Full Name")
        binding.usernameSignup.setHint("User Name")
        binding.emailSignup.setHint("Email")
        binding.passwordSignup.setHint("Password")

        binding.signinLinkBtn.setOnClickListener{
            startActivity(Intent(this,SignInActivity::class.java))
        }

        binding.signupBtn.setOnClickListener {
            createAccount()
        }

    }

    private fun createAccount() {

        val fullname = binding.fullnameSignup.text.toString()
        val username = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()

        when{
            TextUtils.isEmpty(fullname) -> Toast.makeText(this,"full name is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(username) -> Toast.makeText(this,"user name is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,"email is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"password is required", Toast.LENGTH_SHORT).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please Wait, This may take a while...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                        {
                            saveUserInfo(fullname,username,email,progressDialog)
                        }
                        else
                        {
                            val message = task.exception.toString()
                            Toast.makeText(this,"DB Error: $message", Toast.LENGTH_SHORT).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }

        }
    }

    private fun saveUserInfo(fullname: String, username: String, email: String,progressDialog: ProgressDialog) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullname.toLowerCase()
        userMap["username"] = username.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "Hay nice to have you here, Thanks for using MyGram App."
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/mygramapp-40074.appspot.com/o/Default%20Images%2Fprofile%20(1).png?alt=media&token=11be7376-e9f7-4f7e-8cca-781894abd99a"


        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Account has been created successfully", Toast.LENGTH_SHORT).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserId)
                        .child("Following").child(currentUserId)
                        .setValue(true)

                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val message = task.exception.toString()
                    Toast.makeText(this,"DB Error: $message", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }


}
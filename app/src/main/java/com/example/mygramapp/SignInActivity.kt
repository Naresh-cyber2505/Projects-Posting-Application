package com.example.mygramapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.mygramapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.emailLogin.setHint("Email")
        binding.passwordLogin.setHint("Password")

        binding.signupLinkBtn.setOnClickListener{
            startActivity(Intent(this,SignUpActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            loginuser()
        }

    }

    private fun loginuser() {
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()

        when{
            TextUtils.isEmpty(email) -> Toast.makeText(this,"email is required", Toast.LENGTH_SHORT).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"password is required", Toast.LENGTH_SHORT).show()

            else->{
                val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("Login")
                progressDialog.setMessage("Please Wait, This may take a while...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        progressDialog.dismiss()

                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
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

    }

    override fun onStart() {
        super.onStart()

        if(FirebaseAuth.getInstance().currentUser != null)
        {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
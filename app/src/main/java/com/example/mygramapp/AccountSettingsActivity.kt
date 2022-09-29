package com.example.mygramapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mygramapp.databinding.ActivityAccountSettingsBinding
import com.example.mygramapp.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*
import kotlin.collections.HashMap


class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUri = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    private val GELLERY_REQUEST_CODE = 123
    private val WRITE_EXTERNAL_STORAGE_CODE = 1

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this,"Logged Out", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

//        binding.changeImageTextBtn.setOnClickListener {
//            checker = "clicked"
//
//            checkPermission()
//            requestPermission()
//        }

        binding.changeImageTextBtn.setOnClickListener {
            checker = "clicked"

            if (checkPermission()){
                pickFromGallery()
            }
            else
            {
                Toast.makeText(this,"Allow all permissions", Toast.LENGTH_SHORT).show()
                requestPermission()
            }
        }


        binding.saveProfileBtn.setOnClickListener {
            if (checker == "clicked")
            {
                uploadImageAndUpdateImg()
            }
            else
            {
                updateUserInfoOnly()
            }
        }

        userInfo()

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode)
        {
            GELLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                }

                else
                {

                }
            }
        }
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri :Uri ?= UCrop.getOutput(data!!)

//            setImage(resultUri!!)

            imageUri=resultUri

            binding.profileImage.setImageURI(imageUri)


        }
    }






    private fun updateUserInfoOnly()
    {
        when {
            binding.fullNameProfile.text.toString() == "" -> {
                Toast.makeText(this,"Please write full name please", Toast.LENGTH_SHORT).show()
            }
            binding.usernameProfileFrag.text.toString() == "" -> {
                Toast.makeText(this,"Please write user name first", Toast.LENGTH_SHORT).show()
            }
            binding.bioProfileFrag.text.toString() == "" -> {
                Toast.makeText(this,"Please write bio first", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val usersRef = FirebaseDatabase.getInstance().reference
                    .child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = binding.fullNameProfile.text.toString().toLowerCase()
                userMap["username"] = binding.usernameProfileFrag.text.toString().toLowerCase()
                userMap["bio"] = binding.bioProfileFrag.text.toString().toLowerCase()

                usersRef.child(firebaseUser.uid).updateChildren(userMap)
                Toast.makeText(this,"Account Info has been updated", Toast.LENGTH_SHORT).show()

                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }

    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .child(firebaseUser.uid)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {

                if (snapshot.exists())
                {
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(binding.profileImage)

                    binding.usernameProfileFrag.setText(user.getUsername())
                    binding.fullNameProfile.setText(user.getFullname())
                    binding.bioProfileFrag.setText(user.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    private fun uploadImageAndUpdateImg()
    {
        when
        {
            imageUri  == null -> Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show()
            binding.fullNameProfile.text.toString() == "" -> {
                Toast.makeText(this, "Please write full name please", Toast.LENGTH_SHORT).show()
            }
            binding.usernameProfileFrag.text.toString() == "" -> {
                Toast.makeText(this, "Please write user name first", Toast.LENGTH_SHORT).show()
            }
            binding.bioProfileFrag.text.toString() == "" -> {
                Toast.makeText(this, "Please write bio first", Toast.LENGTH_SHORT).show()
            }

            else -> {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait, we are updating your profile...")
                progressDialog.show()


                val fileref = storageProfilePicRef!!.child(firebaseUser.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileref.putFile(imageUri!!)

                uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener( OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful)
                    {
                        val downloadUrl = task.result
                        myUri = downloadUrl.toString()
//                        Toast.makeText(this,downloadUrl.toString(), Toast.LENGTH_SHORT).show()
                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] = binding.fullNameProfile.text.toString().toLowerCase()
                        userMap["username"] = binding.usernameProfileFrag.text.toString().toLowerCase()
                        userMap["bio"] = binding.bioProfileFrag.text.toString().toLowerCase()
                        userMap["image"] = myUri

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this,"Account Info has been updated successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    }
                    else
                    {
                        progressDialog.dismiss()
                    }
                })
            }

        }
    }

    private fun launchImageCrop(uri: Uri){

        var destination:String = StringBuilder(UUID.randomUUID().toString()).toString()
        var options: UCrop.Options = UCrop.Options()

        UCrop.of(Uri.parse(uri.toString()), Uri.fromFile(File(cacheDir,destination)))
            .withOptions(options)
            .withAspectRatio(1F,1F)
            .useSourceImageAspectRatio()
            .withMaxResultSize(2000,2000)
            .start(this)
    }


    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA

            ),
            100
        )
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GELLERY_REQUEST_CODE)

    }
}
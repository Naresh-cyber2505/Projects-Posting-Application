package com.example.mygramapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.mygramapp.databinding.ActivityAddPostBinding
import com.example.mygramapp.fragments.PostDetailsFragment
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

class AddPostActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddPostBinding
    private var myUri = ""
    private var imageUri: Uri? = null
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    private var storageProfilePicRef: StorageReference? = null
    private val GELLERY_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId","none").toString()
        }


        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Posts Pictures")

        binding.saveAddPostBtn.setOnClickListener {
            uploadImage()
        }

        binding.addImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivityForResult(intent, GELLERY_REQUEST_CODE)
        }

        userInfo()


    }

    private fun uploadImage() {
        when{
            imageUri  == null -> Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show()
            binding.descriptionPost.text.toString() == "" -> {
                Toast.makeText(this, "Please write something.", Toast.LENGTH_SHORT).show()

            }
            else ->{

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Post")
                progressDialog.setMessage("Please wait, we are profile...")
                progressDialog.show()

                val fileref = storageProfilePicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

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

                        val ref = FirebaseDatabase.getInstance().reference.child("Posts")
                        val postid = ref.push().key

                        val postMap = HashMap<String, Any>()
                        postMap["postid"] = postid!!
                        postMap["description"] = binding.descriptionPost.text.toString().toLowerCase()
                        postMap["publisher"] = FirebaseAuth.getInstance().currentUser!!.uid
                        postMap["postimage"] = myUri
                        postMap["postedAt"] = Date().time

                        ref.child(postid).updateChildren(postMap)

                        Toast.makeText(this,"Your new post has been uploaded successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
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

    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .child(profileId)

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

                    binding.name.text = user.getFullname()
                    binding.profesion.text = user.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun launchImageCrop(uri: Uri){

        var destination:String = StringBuilder(UUID.randomUUID().toString()).toString()
        var options: UCrop.Options = UCrop.Options()

        UCrop.of(Uri.parse(uri.toString()), Uri.fromFile(File(cacheDir,destination)))
            .withOptions(options)
            .withAspectRatio(2F,1F)
            .useSourceImageAspectRatio()
            .withMaxResultSize(2000,2000)
            .start(this)
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

            binding.imagePost.setImageURI(imageUri)


        }
    }
}
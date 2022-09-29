package com.example.mygramapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.example.mygramapp.databinding.ActivityAddPostBinding
import com.example.mygramapp.databinding.ActivityAddStoryBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.*

class AddStoryActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddStoryBinding
    private var myUri = ""
    private var imageUri: Uri? = null
    private var storageStoryPicRef: StorageReference? = null

    private val GELLERY_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageStoryPicRef = FirebaseStorage.getInstance().reference.child("Story Pictures")


        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GELLERY_REQUEST_CODE)


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

//            binding.imagePost.setImageURI(imageUri)

            uploadStory()

        }
    }

    private fun uploadStory()
    {
        when{
            imageUri  == null -> Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show()

            else->
            {

                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Adding New Story")
                progressDialog.setMessage("Please wait, we are Adding your story...")
                progressDialog.show()

                val fileref = storageStoryPicRef!!.child(System.currentTimeMillis().toString() + ".jpg")

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

                        val ref = FirebaseDatabase.getInstance().reference.child("Story")
                        val storyid = (ref.push().key).toString()

                        val timeEnd = System.currentTimeMillis() + 86400000 // one day later

                        val storyMap = HashMap<String, Any>()
                        storyMap["userid"] = FirebaseAuth.getInstance().currentUser!!.uid
                        storyMap["timestart"] = ServerValue.TIMESTAMP
                        storyMap["timeend"] = timeEnd
                        storyMap["imageurl"] = myUri
                        storyMap["storyid"] = storyid

                        ref.child(storyid).updateChildren(storyMap)

                        Toast.makeText(this,"Your new story has been uploaded successfully", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
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
}
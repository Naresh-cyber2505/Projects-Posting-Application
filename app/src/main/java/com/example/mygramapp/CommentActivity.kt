package com.example.mygramapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mygramapp.adaptor.CommentsAdapter
import com.example.mygramapp.databinding.ActivityCommentBinding
import com.example.mygramapp.model.Comments
import com.example.mygramapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CommentActivity : AppCompatActivity() {

    private var postId = ""
    private var publisherId = ""
    private var firebaseUser: FirebaseUser? = null
    private lateinit var binding: ActivityCommentBinding
    private var commentsAdapter: CommentsAdapter? = null
    private var commentsList: MutableList<Comments>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        publisherId = intent.getStringExtra("publisherId").toString()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        binding.recyclerViewComments.layoutManager = linearLayoutManager

        commentsList = ArrayList()
        commentsAdapter = CommentsAdapter(this, commentsList)
        binding.recyclerViewComments.adapter = commentsAdapter

        userInfo()
        readComments()
        getPostIamge()

        binding.postComment.setOnClickListener(View.OnClickListener {
            if (binding.addComment!!.text.toString() == "")
            {
                Toast.makeText(this@CommentActivity,"Please write comment first", Toast.LENGTH_LONG)
            }
            else
            {
                addComment()
            }
        })

    }

    private fun addComment()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Comments")
            .child(postId)

        val commentMap = HashMap<String, Any>()
        commentMap["comment"] = binding.addComment.text.toString()
        commentMap["publisher"] = firebaseUser!!.uid

        usersRef.push().setValue(commentMap)

        addNotification()

        binding.addComment!!.text.clear()
    }

    private fun userInfo()
    {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .child(firebaseUser!!.uid)

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
                        .into(binding.profileImageComment)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getPostIamge()
    {
        val postRef = FirebaseDatabase.getInstance().getReference()
            .child("Posts")
            .child(postId).child("postimage")

        postRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {

                if (snapshot.exists())
                {
                    val image = snapshot.value.toString()

                    Picasso.get()
                        .load(image)
                        .placeholder(R.drawable.profile)
                        .into(binding.postImageComment)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun readComments()
    {
        val commentsRef = FirebaseDatabase.getInstance()
            .reference.child("Comments")
            .child(postId)

        commentsRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists())
                {
                    commentsList!!.clear()

                    for (snapshot in p0.children)
                    {
                        val comments = snapshot.getValue(Comments::class.java)
                        commentsList!!.add(comments!!)
                    }

                    commentsAdapter!!.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addNotification()
    {
        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(publisherId!!)

        val notifMap = HashMap<String, Any>()
        notifMap["userid"] = firebaseUser!!.uid
        notifMap["text"] = "commented : " + binding.addComment.text.toString()
        notifMap["postid"] = postId
        notifMap["ispost"] = true

        notiRef.push().setValue(notifMap)

    }
}
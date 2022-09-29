package com.example.mygramapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mygramapp.adaptor.UserAdaptor
import com.example.mygramapp.databinding.ActivityShowUsersBinding
import com.example.mygramapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShowUsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShowUsersBinding
    var id: String = ""
    var title: String = ""

    var userAdaptor: UserAdaptor? = null
    var userList: List<User>? = null
    var idList: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        id = intent.getStringExtra("id").toString()
        title = intent.getStringExtra("title").toString()

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdaptor = UserAdaptor(this, userList as ArrayList<User>, false)
        binding.recyclerView.adapter = userAdaptor


        idList = ArrayList()

        when(title)
        {
            "likes" -> getLikes()
            "following" -> getFollowing()
            "followers" -> getFollowers()
            "views" -> getViews()
        }



    }

    private fun getViews() {


    }

    private fun getFollowers() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (idList as ArrayList<String>).clear()

                for (snapshot in snapshot.children)
                {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUers()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getFollowing() {

        val followersRef = FirebaseDatabase.getInstance().reference
            .child("Follow").child(id!!)
            .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                (idList as ArrayList<String>).clear()

                for (snapshot in snapshot.children)
                {
                    (idList as ArrayList<String>).add(snapshot.key!!)
                }
                showUers()

            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getLikes() {
        val LikeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(id!!)

        LikeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){

                    (idList as ArrayList<String>).clear()

                    for (snapshot in p0.children)
                    {
                        (idList as ArrayList<String>).add(snapshot.key!!)
                    }
                    showUers()
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showUers() {

        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(datasnapshot: DataSnapshot)
            {

                (userList as ArrayList<User>).clear()

                for (snapshot in datasnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)

                    for (id in idList!!)
                    {
                        if (user!!.getUid() == id)
                        {
                            (userList as ArrayList<User>).add(user!!)
                        }
                    }
                }
                userAdaptor?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}
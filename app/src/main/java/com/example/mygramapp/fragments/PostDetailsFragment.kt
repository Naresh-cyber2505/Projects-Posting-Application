package com.example.mygramapp.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.R
import com.example.mygramapp.adaptor.PostAdapter
import com.example.mygramapp.databinding.FragmentPostDetailsBinding
import com.example.mygramapp.model.Post
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PostDetailsFragment : Fragment() {

    private lateinit var binding:FragmentPostDetailsBinding
    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var postId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPostDetailsBinding.inflate(layoutInflater, container,false)

        val preferences = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (preferences!=null)
        {
            postId = preferences.getString("postId", "none").toString()
        }

//        var recyclerView: RecyclerView
//        recyclerView = view!!.findViewById(R.id.recycler_view_post_details)
        binding.recyclerViewPostDetails.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(context)
        binding.recyclerViewPostDetails.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it,postList as ArrayList<Post>) }
        binding.recyclerViewPostDetails.adapter = postAdapter


        readPost()

        return binding.root;
    }

    private fun readPost() {

        val postsRef =
            FirebaseDatabase.getInstance().reference.child("Posts")
                .child(postId)

        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                postList?.clear()

                val post = p0.getValue(Post::class.java)

                postList!!.add(post!!)

                postAdapter!!.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


}
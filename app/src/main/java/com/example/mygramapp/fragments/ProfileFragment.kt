package com.example.mygramapp.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.AccountSettingsActivity
import com.example.mygramapp.R
import com.example.mygramapp.ShowUsersActivity
import com.example.mygramapp.adaptor.MyImagesAdapter
import com.example.mygramapp.databinding.FragmentProfileBinding
import com.example.mygramapp.model.Post
import com.example.mygramapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileId: String
    private lateinit var firebaseUser: FirebaseUser

    var postList: List<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null

    var myImagesAdapterSavedImg: MyImagesAdapter? = null
    var postListSaved: List<Post>? = null
    var mySavedImg: List<String>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater,container,false)
        val view = binding.root

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        if (pref != null)
        {
            this.profileId = pref.getString("profileId","none").toString()
        }

        if (profileId == firebaseUser.uid)
        {
            binding.editAccountBtn.text = "Edit Profile"
        }
        else if (profileId != firebaseUser.uid)
        {
            checkFollowAndFollowingButtonStatus()
        }


        //recycler view for uploded images
        var recyclerViewUploadedImage: RecyclerView
        recyclerViewUploadedImage = view.findViewById(R.id.recycler_view_upload_pic)
        recyclerViewUploadedImage.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewUploadedImage.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = context?.let { MyImagesAdapter(it,postList as ArrayList<Post>) }
        recyclerViewUploadedImage.adapter = myImagesAdapter


        //recycler view for saved images
        var recyclerViewSavedImage: RecyclerView
        recyclerViewSavedImage = view.findViewById(R.id.recycler_view_saved_pic)
        recyclerViewSavedImage.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(context, 3)
        recyclerViewSavedImage.layoutManager = linearLayoutManager2

        postListSaved = ArrayList()
        myImagesAdapterSavedImg = context?.let { MyImagesAdapter(it,postListSaved as ArrayList<Post>) }
        recyclerViewSavedImage.adapter = myImagesAdapterSavedImg

        recyclerViewSavedImage.visibility = View.GONE
        recyclerViewUploadedImage.visibility = View.VISIBLE

        var uploadImagesBtn: ImageButton
        uploadImagesBtn = view.findViewById(R.id.image_grid_view_btn)
        uploadImagesBtn.setOnClickListener {
            recyclerViewSavedImage.visibility = View.GONE
            recyclerViewUploadedImage.visibility = View.VISIBLE
        }

        var savedImagesBtn: ImageButton
        savedImagesBtn = view.findViewById(R.id.image_save_btn)
        savedImagesBtn.setOnClickListener {
            recyclerViewSavedImage.visibility = View.VISIBLE
            recyclerViewUploadedImage.visibility = View.GONE
        }

        binding.totalFollowers.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "followers")
            startActivity(intent)
        }

        binding.totalFollowing.setOnClickListener {
            val intent = Intent(context, ShowUsersActivity::class.java)
            intent.putExtra("id", profileId)
            intent.putExtra("title", "following")
            startActivity(intent)
        }



        binding.editAccountBtn.setOnClickListener{

            val getButtonText = binding.editAccountBtn.text.toString()

            when
            {
                getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingsActivity::class.java))

                getButtonText == "Follow" -> {

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .setValue(true)
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .setValue(true)
                    }
                    addNotification()
                }


                getButtonText == "Following" -> {
                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(it1.toString())
                            .child("Following").child(profileId)
                            .removeValue()
                    }

                    firebaseUser.uid.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Follow").child(profileId)
                            .child("Followers").child(it1.toString())
                            .removeValue()
                    }
                }

            }
//            startActivity(Intent(context, AccountSettingsActivity::class.java))

        }

        getFollowers()
        getFollowings()
        userInfo()
        myPhotos()

        mySave()



        getTotalNumberOfPosts()

        return view
    }

    private fun checkFollowAndFollowingButtonStatus()
    {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

            if (followingRef != null)
            {
                followingRef.addValueEventListener(object : ValueEventListener
                {
                    override fun onDataChange(snapshot: DataSnapshot)
                    {
                        if (snapshot.child(profileId).exists())
                        {
                            binding.editAccountBtn.text = "Following"
                        }
                        else
                        {
                            binding.editAccountBtn.text = "Follow"
                        }
                    }

                    override fun onCancelled(error: DatabaseError)
                    {

                    }
                })
            }
    }

    private fun getFollowers()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                if(snapshot.exists())
                {
                    binding.totalFollowers?.text = snapshot.childrenCount.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getFollowings()
    {
        val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")

        followersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                if(snapshot.exists())
                {
                    binding.totalFollowing?.text = snapshot.childrenCount.toString()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun myPhotos()
    {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")

        postRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()){
                    (postList as ArrayList<Post>).clear()

                    // if the loggedin user post match with the post publisherid then it means it is the post
                    for (snapshot in p0.children)
                    {
                        val post = snapshot.getValue(Post::class.java)
                        if (post!!.getPublisher().equals(profileId))
                        {
                            (postList as ArrayList<Post>).add(post)
                        }

                        Collections.reverse(postList)
                        myImagesAdapter!!.notifyDataSetChanged()
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
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
//                if (context != null)
//                {
//                    return
//                }

                if (snapshot.exists())
                {
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(binding.profileImage)

                    binding.profileFragmentUsername.text = user.getUsername()
                    binding.fullNameProfileFrag.text = user.getFullname()
                    binding.bioProfileFrag.text = user.getBio()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onStop() {
        super.onStop()

        val  pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",  firebaseUser.uid)
        pref?.apply()
    }

    override fun onPause() {
        super.onPause()

        val  pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",  firebaseUser.uid)
        pref?.apply()
    }

    override fun onDestroy() {
        super.onDestroy()

        val  pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
        pref?.putString("profileId",  firebaseUser.uid)
        pref?.apply()
    }

    private fun getTotalNumberOfPosts()
    {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists())
                {
                    var postCounter = 0

                    for (snapShot in dataSnapshot.children)
                    {
                        val post = snapShot.getValue(Post::class.java)!!
                        if (post.getPublisher() == profileId)
                        {
                            postCounter++
                        }
                    }
                    binding.totalPosts.text = " " + postCounter
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun mySave()
    {
        mySavedImg = ArrayList()

        val savedRef = FirebaseDatabase.getInstance()
            .reference
            .child("Saves").child(firebaseUser!!.uid)

        savedRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists())
                {
                    for (snapshot in dataSnapshot.children)
                    {
                        (mySavedImg as ArrayList<String>).add(snapshot.key!!)
                    }
                    readSavedImages()
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun readSavedImages() {

        val postRef = FirebaseDatabase.getInstance()
            .reference
            .child("Posts")

        postRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(datasnapshot: DataSnapshot) {
                if (datasnapshot.exists()){

                    (postListSaved as ArrayList<Post>).clear()
                    for (snapshot in datasnapshot.children){
                        val post = snapshot.getValue(Post::class.java)

                        for (key in mySavedImg!!)
                        {
                            if (post!!.getPostid() == key)
                            {
                                (postListSaved as ArrayList<Post>).add(post!!)
                            }
                        }
                    }
                    myImagesAdapterSavedImg!!.notifyDataSetChanged()
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
            .child(profileId)

        val notifMap = HashMap<String, Any>()
        notifMap["userid"] = firebaseUser!!.uid
        notifMap["text"] = "started following you"
        notifMap["postid"] = ""
        notifMap["ispost"] = false

        notiRef.push().setValue(notifMap)

    }

}
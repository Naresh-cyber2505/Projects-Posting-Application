package com.example.mygramapp.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.R
import com.example.mygramapp.fragments.PostDetailsFragment
import com.example.mygramapp.fragments.ProfileFragment
import com.example.mygramapp.model.Notification
import com.example.mygramapp.model.Post
import com.example.mygramapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class NotificationAdapter (
    private val mContext: Context,
    private val mNotification: List<Notification>)
    : RecyclerView.Adapter<NotificationAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val View = LayoutInflater.from(mContext).inflate(R.layout.notification_item_layout, parent, false)
        return ViewHolder(View)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        if (notification.getText().equals("started following you"))
        {
            holder.text.text = "started following you"
        }
        else if (notification.getText().equals("liked your post"))
        {
            holder.text.text = "liked your post"
        }
        else if (notification.getText().contains("commented:"))
        {
            holder.text.text = notification.getText().replace("commented:", "commented: ")
        }
        else
        {
            holder.text.text = notification.getText()
        }





        userInfo(holder.profileIamge, holder.userName, notification.getUserid())

        if (notification.isIsPost())
        {
            holder.postIamge.visibility = View.VISIBLE
            getPostIamge(holder.postIamge, notification.getPostId())
        }
        else
        {
            holder.postIamge.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            if (notification.isIsPost())
            {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

                editor.putString("postId", notification.getPostId())

                editor.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment()).commit()
            }
            else
            {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

                editor.putString("profileId", notification.getUserid())

                editor.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }
    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var postIamge: ImageView
        var profileIamge: CircleImageView
        var userName: TextView
        var text: TextView

        init {
            postIamge = itemView.findViewById(R.id.notification_post_image)
            profileIamge = itemView.findViewById(R.id.notification_profile_image)
            userName = itemView.findViewById(R.id.username_notification)
            text = itemView.findViewById(R.id.comment_notification)
        }
    }


    private fun userInfo(imageView: ImageView, userName: TextView, publisher: String)
    {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .child(publisher)

        usersRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {

                if (snapshot.exists())
                {
                    val user = snapshot.getValue<User>(User::class.java)

                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(imageView)

                    userName.text = user!!.getUsername()



                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getPostIamge(imageView: ImageView, postId: String)
    {
        val postRef = FirebaseDatabase.getInstance().getReference()
            .child("Posts")
            .child(postId)

        postRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {

                if (snapshot.exists())
                {
                    val post = snapshot.getValue(Post::class.java)

                    Picasso.get()
                        .load(post!!.getPostimage())
                        .placeholder(R.drawable.profile)
                        .into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}
package com.example.mygramapp.adaptor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.MainActivity
import com.example.mygramapp.R
import com.example.mygramapp.fragments.PostDetailsFragment
import com.example.mygramapp.fragments.ProfileFragment
import com.example.mygramapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdaptor (private var mContext: Context,
                    private var mUser: List<User>,
                    private var isFragment: Boolean = false) : RecyclerView.Adapter<UserAdaptor.ViewHolder>()
{

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdaptor.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)

        return UserAdaptor.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val user = mUser[position]

        holder.userNameText.text = user.getUsername()
        holder.userFullNameText.text = user.getFullname()
        Picasso.get()
            .load(user.getImage())
            .placeholder(R.drawable.profile)
            .into(holder.userProfileImage)

        checkFollowingStatus(user.getUid(), holder.followButton)

        holder.itemView.setOnClickListener(View.OnClickListener {
            if (isFragment)
            {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()

                editor.putString("profileId", user.getUid())

                editor.apply()

                (mContext as FragmentActivity).supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
            else
            {
                val intent = Intent(mContext, MainActivity::class.java)
                intent.putExtra("publisherId", user.getUid())
                    mContext.startActivity(intent)
            }
        })

        holder.followButton.setOnClickListener {
            if (holder.followButton.text.toString() == "Follow")
            {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .setValue(true).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .setValue(true).addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
                addNotification(user.getUid())
            }
            else
            {
                firebaseUser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(it1.toString())
                        .child("Following").child(user.getUid())
                        .removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                firebaseUser?.uid.let { it1 ->
                                    FirebaseDatabase.getInstance().reference
                                        .child("Follow").child(user.getUid())
                                        .child("Followers").child(it1.toString())
                                        .removeValue().addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                            }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    class ViewHolder (@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var userNameText: TextView = itemView.findViewById(R.id.user_name_post)
        var userFullNameText: TextView = itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_post)
        var followButton: Button = itemView.findViewById(R.id.follow_btn_search)
    }

    private fun checkFollowingStatus(uid: String, followButton: Button)
    {
        val followingRef = firebaseUser?.uid.let { it1 ->
            FirebaseDatabase.getInstance().reference
                .child("Follow").child(it1.toString())
                .child("Following")
        }

        followingRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(datasnapshot: DataSnapshot)
            {
                if (datasnapshot.child(uid).exists())
                {
                    followButton.text = "Following"
                }
                else
                {
                    followButton.text = "Follow"
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun addNotification(userId: String)
    {
        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(userId)

        val notifMap = HashMap<String, Any>()
        notifMap["userid"] = firebaseUser!!.uid
        notifMap["text"] = "started following you"
        notifMap["postid"] = ""
        notifMap["ispost"] = false

        notiRef.push().setValue(notifMap)

    }

}
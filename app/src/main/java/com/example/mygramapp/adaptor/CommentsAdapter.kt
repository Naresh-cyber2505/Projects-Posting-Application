package com.example.mygramapp.adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.R
import com.example.mygramapp.model.Comments
import com.example.mygramapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentsAdapter(private val mContext: Context, private val mComments: MutableList<Comments>?
) : RecyclerView.Adapter<CommentsAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsAdapter.ViewHolder {
        val View = LayoutInflater.from(mContext).inflate(R.layout.comments_layout, parent, false)
        return ViewHolder(View)
    }

    override fun onBindViewHolder(holder: CommentsAdapter.ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val comment = mComments!![position]
        holder.commentTV.text = comment.getComment()
        getUserInfo(holder.imageProfile, holder.userNameTV, comment.getPublisher())
    }

    private fun getUserInfo(imageProfile: CircleImageView, userNameTV: TextView, publisher: String) {
        val userRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(publisher)

        userRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists())
                {
                    val user = p0.getValue(User::class.java)
                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(imageProfile)

                    userNameTV.text = user.getFullname()


                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    override fun getItemCount(): Int {
        return mComments!!.size
    }

    inner class ViewHolder(@NonNull itemView: View ) : RecyclerView.ViewHolder(itemView)
    {
        var imageProfile: CircleImageView
        var userNameTV: TextView
        var commentTV: TextView

        init {
            imageProfile = itemView.findViewById(R.id.user_profile_image_comment)
            userNameTV = itemView.findViewById(R.id.user_name_comment)
            commentTV = itemView.findViewById(R.id.comment_comment)
        }
    }

}
package com.example.mygramapp.adaptor

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.AddStoryActivity
import com.example.mygramapp.MainActivity
import com.example.mygramapp.R
import com.example.mygramapp.model.Story
import com.example.mygramapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class StoryAdapter (private val mContext: Context, private val mStory: List<Story>)
    : RecyclerView.Adapter<StoryAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return if (viewType == 0)
        {
            val view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false)
             ViewHolder(view)
        }
        else
        {
            val view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false)
             ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        val story = mStory[position]

        userInfo(holder, story.getUserId(), position)

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, AddStoryActivity::class.java)
            intent.putExtra("userId", story.getUserId())
            mContext.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return mStory.size
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){

        // storyitem
        var story_image_seen: CircleImageView? = null
        var story_image_: CircleImageView? = null
        var story_username: TextView? = null

        //addstoryitem
        var story_plus_btn: ImageView? = null
        var addStory_text: TextView? = null

        init {
            story_image_seen = itemView.findViewById(R.id.story_image_seen)
            story_image_ = itemView.findViewById(R.id.story_image)
            story_username = itemView.findViewById(R.id.story_user_name)

            //addstoryitem
            story_plus_btn = itemView.findViewById(R.id.story_add)
            addStory_text = itemView.findViewById(R.id.add_story_text)

        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
        {
            return 0
        }
        return 1
    }

    private fun userInfo(viewHolder: ViewHolder, userId: String, position: Int)
    {
        val usersRef = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .child(userId)

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
                        .into(viewHolder.story_image_)

                    if (position != 0)
                    {
                        Picasso.get()
                            .load(user!!.getImage())
                            .placeholder(R.drawable.profile)
                            .into(viewHolder.story_image_seen)
                        viewHolder.story_username!!.text = user.getUsername()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}
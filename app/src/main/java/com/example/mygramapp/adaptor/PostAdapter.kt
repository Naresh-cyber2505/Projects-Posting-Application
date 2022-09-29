package com.example.mygramapp.adaptor

import android.content.Context
import android.content.Intent
import android.content.LocusId
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.CommentActivity
import com.example.mygramapp.MainActivity
import com.example.mygramapp.R
import com.example.mygramapp.ShowUsersActivity
import com.example.mygramapp.model.Post
import com.example.mygramapp.model.User
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser = FirebaseAuth.getInstance().currentUser

        val post = mPost[position]

        Picasso.get()
            .load(post.getPostimage())
            .into(holder.postImage)

        val text = post.getPostedAt()?.let { TimeAgo.using(it) }
        holder.time.text = text


        if (post.getDescription().equals(""))
        {
            holder.description.visibility = View.GONE
        }
        else
        {
            holder.description.visibility = View.VISIBLE
            holder.description.setText(post.getDescription())
        }

        publisherInfo(holder.profileImage, holder.useName, holder.publisher, post.getPublisher())

        // likes
        isLikes(post.getPostid(), holder.likeButton)

        numberOfLikes(holder.likes, post.getPostid())

        getTotalComments(holder.comments, post.getPostid())

        checkSavedStatus(post.getPostid(), holder.saveButton)
        
        holder.likeButton.setOnClickListener { 
            if (holder.likeButton.tag == "Like")
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .setValue(true)

                addNotification(post.getPublisher(), post.getPostid())
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("Likes")
                    .child(post.getPostid())
                    .child(firebaseUser!!.uid)
                    .removeValue()

                // refereshes the page
                val intent = Intent(mContext, MainActivity::class.java)
//                mContext.startActivity(intent)
            }
        }

        holder.likes.setOnClickListener {
            val intent = Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id", post.getPostid())
            intent.putExtra("title", "likes")
            mContext.startActivity(intent)
        }

        holder.commentButten.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener {
            val intentComment = Intent(mContext, CommentActivity::class.java)
            intentComment.putExtra("postId", post.getPostid())
            intentComment.putExtra("publisherId", post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.saveButton.setOnClickListener {
            if (holder.saveButton.tag == "Save")
            {
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser!!.uid)
                    .child(post.getPostid())
                    .setValue(true)
            }
            else
            {
                FirebaseDatabase.getInstance().reference
                    .child("Saves")
                    .child(firebaseUser!!.uid).child(post.getPostid())
                    .removeValue()
            }
        }

    }

    private fun numberOfLikes(likes: TextView, postid: String) {

        val LikeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postid)

        LikeRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    likes.text = snapshot.childrenCount.toString() + " likes"
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun getTotalComments(comments: TextView, postid: String) {

        val commentRef = FirebaseDatabase.getInstance().reference
            .child("Comments").child(postid)

        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    comments.text = "View all " + snapshot.childrenCount.toString() + " comments"
                }

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }

    private fun isLikes(postId: String, likeButton: ImageView) {

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val LikeRef = FirebaseDatabase.getInstance().reference
            .child("Likes").child(postId)

        LikeRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(firebaseUser!!.uid).exists())
                {
                    likeButton.setImageResource(R.drawable.love)
                    likeButton.tag = "Liked"
                }
                else
                {
                    likeButton.setImageResource(R.drawable.heart)
                    likeButton.tag = "Like"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){

        var profileImage: CircleImageView
        var postImage: ImageView
        var likeButton: ImageView
        var commentButten: ImageView
        var saveButton: ImageView
        var useName: TextView
        var likes: TextView
        var publisher: TextView
        var description: TextView
        var comments: TextView
        var time: TextView

        init {
            profileImage = itemView.findViewById(R.id.user_profile_image_post)
            postImage = itemView.findViewById(R.id.post_image_home)
            likeButton = itemView.findViewById(R.id.post_image_like_btn)
            commentButten = itemView.findViewById(R.id.post_image_comment_btn)
            saveButton = itemView.findViewById(R.id.post_save_comment_btn)
            useName = itemView.findViewById(R.id.user_name_post)
            likes = itemView.findViewById(R.id.likes)
            publisher = itemView.findViewById(R.id.publisher)
            description = itemView.findViewById(R.id.description)
            comments = itemView.findViewById(R.id.comments)
            time = itemView.findViewById(R.id.time)
        }
    }

    private fun publisherInfo(profileImage: CircleImageView, useName: TextView, publisher: TextView, publisherId: String) {

        val userRef = FirebaseDatabase.getInstance().reference.child("Users").child(publisherId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(User::class.java)

                    Picasso.get()
                        .load(user!!.getImage())
                        .placeholder(R.drawable.profile)
                        .into(profileImage)

                    useName.text = user!!.getUsername()
                    publisher.text = user!!.getFullname()

                }
            }

            override fun onCancelled(error: DatabaseError) {


            }
        })
    }

    private fun checkSavedStatus(postid: String, imageView: ImageView)
    {
        val saveRef = FirebaseDatabase.getInstance().reference
            .child("Saves")
            .child(firebaseUser!!.uid)

        saveRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {

                if (p0.child(postid).exists())
                {
                    imageView.setImageResource(R.drawable.save)
                    imageView.tag = "Saved"
                }
                else
                {
                    imageView.setImageResource(R.drawable.ribbon)
                    imageView.tag = "Save"
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

    }

    private fun addNotification(userId: String, postId: String)
    {
        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(userId)

        val notifMap = HashMap<String, Any>()
        notifMap["userid"] = firebaseUser!!.uid
        notifMap["text"] = "liked your post"
        notifMap["postid"] = postId
        notifMap["ispost"] = true

        notiRef.push().setValue(notifMap)

    }

}
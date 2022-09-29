package com.example.mygramapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mygramapp.R
import com.example.mygramapp.adaptor.NotificationAdapter
import com.example.mygramapp.model.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {

//    private lateinit var binding: FragmentNotificationBinding
    private var notificationList: ArrayList<Notification>? = null
    private var notificationAdapter: NotificationAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val  view = inflater.inflate(R.layout.fragment_notification, container, false)

        val recyclerView: RecyclerView
        recyclerView = view!!.findViewById(R.id.recycler_view_notifications)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationList = ArrayList()

        notificationAdapter = NotificationAdapter(requireContext(), notificationList as ArrayList<Notification>)
        recyclerView.adapter = notificationAdapter

        readNotifications()

        return view

    }

    private fun readNotifications() {

        val notiRef = FirebaseDatabase.getInstance()
            .reference.child("Notifications")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)


        notiRef.addValueEventListener(object : ValueEventListener{

            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                if (dataSnapshot.exists())
                {
                    (notificationList as ArrayList<Notification>).clear()

                    for (snapshot in dataSnapshot.children)
                    {

                        try {
                            val notification = snapshot.getValue(Notification::class.java)
                            (notificationList as ArrayList<Notification>).add(notification!!)

                        }
                        catch (e : Exception)
                        {
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }

                    }

                    notificationList?.let { it.reverse() }
                    notificationAdapter!!.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {


            }
        })

    }


}
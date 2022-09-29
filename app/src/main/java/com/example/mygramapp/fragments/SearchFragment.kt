package com.example.mygramapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mygramapp.R
import com.example.mygramapp.adaptor.UserAdaptor
import com.example.mygramapp.databinding.FragmentSearchBinding
import com.example.mygramapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ObjectInput


class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private var mUser: MutableList<User>? = null
    private var userAdaptor: UserAdaptor? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.recyclerViewSearch.setHasFixedSize(true)
        binding.recyclerViewSearch.layoutManager = LinearLayoutManager(context)

        mUser = ArrayList()
        userAdaptor = context?.let { UserAdaptor(it, mUser as ArrayList<User>, true) }
        binding.recyclerViewSearch.adapter = userAdaptor


        binding.searchEditText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (binding.searchEditText.text.toString() == "")
                {

                }
                else
                {
                    binding.recyclerViewSearch.visibility = View.VISIBLE

                    retrieveUsers()
                    searchUser(s.toString().toLowerCase())
                }
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })



        return view
    }



    private fun searchUser(input: String)
    {
        val query = FirebaseDatabase.getInstance().getReference()
            .child("Users")
            .orderByChild("fullname")
            .startAt(input)
            .endAt(input + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(datasnapshot: DataSnapshot)
            {
                mUser?.clear()

                for (snapshot in datasnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null)
                    {
                        mUser?.add(user)
                    }
                }
                userAdaptor?.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    private fun retrieveUsers()
    {
        val userRef = FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(datasnapshot: DataSnapshot)
            {

                if (binding?.searchEditText.text.toString() == "")
                {
                    mUser?.clear()

                    for (snapshot in datasnapshot.children)
                    {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null)
                        {
                            mUser?.add(user)
                        }
                    }
                    userAdaptor?.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


}
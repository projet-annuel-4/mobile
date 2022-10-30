package com.mehdi.myapplication.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mehdi.myapplication.R
import com.mehdi.myapplication.adapter.RecyclerAdapter

class GroupMessageFragment: Fragment() {
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private var idGroup: Long? = null
    private lateinit var userConnectedPreferences : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_post_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProperties()
        loadMessage()
    }

    private fun loadProperties(){
        userConnectedPreferences = this.activity?.getSharedPreferences("Login", Context.MODE_PRIVATE)!!
        idGroup = arguments!!.getLong("idGroup")
    }

    private fun loadMessage(){

    }
}
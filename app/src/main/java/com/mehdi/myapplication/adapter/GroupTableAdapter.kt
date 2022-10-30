package com.mehdi.myapplication.adapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.gson.Gson
import com.mehdi.myapplication.fragment.*
import com.mehdi.myapplication.models.DataModel

class GroupTableAdapter(activity: AppCompatActivity, val group: DataModel.Group) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val args = Bundle()
        args.putString("group", Gson().toJson(group))
        return when (position) {
            0 -> {
                val groupProjectFragment = GroupProjectFragment()
                groupProjectFragment.arguments = args
                groupProjectFragment
            }
            else -> {
                val groupMembersFragment = GroupMembersFragment()
                groupMembersFragment.arguments = args
                groupMembersFragment
            }

        }
    }
}
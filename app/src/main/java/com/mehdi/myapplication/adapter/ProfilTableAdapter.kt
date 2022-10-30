package com.mehdi.myapplication.adapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mehdi.myapplication.fragment.ProfileAnswerFragment
import com.mehdi.myapplication.fragment.ProfileFollowerFragment
import com.mehdi.myapplication.fragment.ProfilePostFragment

class ProfilTableAdapter(activity: AppCompatActivity, val idUser: Long) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val args = Bundle()
        args.putLong("idUser", idUser)
        return when (position) {
            0 -> {
                val profilePostFragment = ProfilePostFragment()
                profilePostFragment.arguments = args
                profilePostFragment
            }
            1 -> {
                val profileAnswerFragment = ProfileAnswerFragment()
                profileAnswerFragment.arguments = args
                profileAnswerFragment
            }
            else -> {
                val profileFriendFragment = ProfileFollowerFragment()
                profileFriendFragment.arguments = args
                profileFriendFragment
            }
        }
    }
}
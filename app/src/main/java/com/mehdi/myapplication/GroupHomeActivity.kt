package com.mehdi.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.group_activity.*
import kotlinx.android.synthetic.main.group_home_activity.*
import kotlinx.android.synthetic.main.group_home_activity_header.*

class GroupHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_home_activity)

        group_home_back_arrow.setOnClickListener {
            finish()
        }

        group_home_create_group.setOnClickListener {
            val intent = Intent(this, GroupActivity::class.java)
            startActivity(intent)
        }
    }
}
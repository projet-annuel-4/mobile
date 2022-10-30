package com.mehdi.myapplication

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.search_group_activity.*
import kotlinx.android.synthetic.main.search_in_site_activity.*

class SearchInSiteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.search_in_site_activity)

        search_in_site_back_arrow.setOnClickListener {
            finish()
        }
    }
}
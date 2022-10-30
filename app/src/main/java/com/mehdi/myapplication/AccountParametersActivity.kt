package com.mehdi.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.account_parameters_activity_header.*
import kotlinx.android.synthetic.main.group_activity.*

class AccountParametersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_parameters_activity)

        account_parameters_back_arrow.setOnClickListener {
            finish()
        }
    }
}
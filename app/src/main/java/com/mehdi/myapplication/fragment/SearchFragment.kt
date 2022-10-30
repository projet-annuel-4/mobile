package com.mehdi.myapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.mehdi.myapplication.SearchPostResultActivity
import com.mehdi.myapplication.SearchUserResultActivity
import com.mehdi.myapplication.models.RequestModel
import kotlinx.android.synthetic.main.create_post_activity.*
import kotlinx.android.synthetic.main.search_fragment.*


class SearchFragment: Fragment() {

    private var tags: MutableList<String> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(com.mehdi.myapplication.R.layout.search_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        search_post_add_tag_button.setOnClickListener {
            addTag()
        }

        search_post_search_action.setOnClickListener {
            searchPost()
        }
        search_user_search_action.setOnClickListener {
            searchUser()
        }

        val rb = search_radio_button as RadioGroup
        rb.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                com.mehdi.myapplication.R.id.radio_post -> {
                    search_post_form.visibility = View.VISIBLE
                    search_user_form.visibility = View.GONE
                }
                com.mehdi.myapplication.R.id.radio_user -> {
                    search_post_form.visibility = View.GONE
                    search_user_form.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun searchUser(){
        if ( search_user_firstname.text.toString().isEmpty() ){
            Toast.makeText(context, "You must specify a firstname", Toast.LENGTH_LONG).show()
            return
        }
        val firstname = search_user_firstname.text.toString()
        val intent = Intent(context, SearchUserResultActivity::class.java)
        intent.putExtra("request", firstname)
        startActivity(intent)
    }

    private fun searchPost(){
        if( search_post_post_title.text.toString().isEmpty() &&
            search_post_post_content.text.toString().isEmpty() &&
            tags.size == 0){
            Toast.makeText(context, "Cannot make a search without at least 1 filter", Toast.LENGTH_LONG).show()
            return
        }
        val request = buildSearchRequest()
        val intent = Intent(context, SearchPostResultActivity::class.java)
        intent.putExtra("request", Gson().toJson(request))
        startActivity(intent)
    }

    private fun addTag(){
        if( search_post_add_tag_value.text.toString().isEmpty()){
            Toast.makeText(context, "Cannot add empty tag", Toast.LENGTH_LONG).show()
            return
        }
        if(tags.size >= 5){
            Toast.makeText(context, "Cannot add more than 5 tag", Toast.LENGTH_LONG).show()
            return
        }
        val tag = search_post_add_tag_value.text.toString()
        for( i in tags){
            if (i == tag){
                Toast.makeText(context, "Already in list", Toast.LENGTH_LONG).show()
                return
            }
        }
        addchip(tag)
        search_post_add_tag_value.text = Editable.Factory.getInstance().newEditable("")
    }

    private fun addchip(tag: String){
        val chip = Chip(context)
        chip.text = tag
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener{
            removeTagFromList(chip.text.toString())
            search_post_tag_chip_group.removeView(chip)
        }
        tags.add(tag)
        search_post_tag_chip_group.addView(chip)
    }

    private fun removeTagFromList(tag: String){
        for( i in tags){
            if (i == tag){
                tags.remove(i)
                return
            }
        }
    }

    private fun buildSearchRequest(): RequestModel.FilterRequest{
        var request: RequestModel.FilterRequest = RequestModel.FilterRequest(ArrayList())
        if ( search_post_post_title.text.toString().isNotEmpty() ){
            request.filters.add(RequestModel.Filter(
                    "title",
                    4,
                    search_post_post_title.text.toString(),
                    ArrayList()
                    )
            )
        }
        if ( search_post_post_content.text.toString().isNotEmpty() ){
            request.filters.add(RequestModel.Filter(
                "content",
                4,
                search_post_post_content.text.toString(),
                ArrayList()
            )
            )
        }
        if( tags.size > 0){
            request.filters.add(RequestModel.Filter(
                "content",
                4,
                "",
                tags
            )
            )
        }
        return request
    }


}
package com.mehdi.myapplication.fragment

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.API.NetworkManager.createProject
import com.mehdi.myapplication.R
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.create_group_activity.*
import kotlinx.android.synthetic.main.group_activity.*
import kotlinx.android.synthetic.main.group_members_fragment.*
import kotlinx.android.synthetic.main.group_project_fragment.*
import kotlinx.android.synthetic.main.profile_post_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupProjectFragment : Fragment() {

    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private var group: DataModel.Group? = null
    private lateinit var userConnectedPreferences : SharedPreferences
    private var projects: MutableList<DataModel.Project> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_project_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        group_project_fragment_form_create_project.visibility = View.GONE

        group_project_fragment_form_project_close.setOnClickListener {
            group_project_fragment_form_create_project.visibility = View.GONE
        }
        group_project_fragment_add_project.setOnClickListener {
            group_project_fragment_form_create_project.visibility = View.VISIBLE
        }

        group_project_fragment_form_create_project_button.setOnClickListener {
            createProject()
        }


        group_project_fragment_add_project
        loadProperties()
        loadProjects()
    }

    private fun loadProperties(){
        userConnectedPreferences = this.activity?.getSharedPreferences("Login", Context.MODE_PRIVATE)!!
        group = Gson().fromJson(arguments!!.getString("group"), DataModel.Group::class.java)
    }

    private fun loadProjects(){
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.ProjectResponse>
            try {

                response = NetworkManager.getProjectFromGroupByGroupId(group!!.id!!, userConnectedPreferences!!.getString("accessToken", null)!!)
                print(response)
                withContext(Dispatchers.Main) {
                    recyclerAdapter.setData(convertResponseToProjectModel(response))
                    group_project_fragment_recycler_view.adapter = recyclerAdapter
                    print(recyclerAdapter.itemCount)
                }

            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    private fun convertResponseToProjectModel(
        data: MutableIterable<ResponseModel.ProjectResponse>
    ): MutableList<DataModel.Project>{

        var projects: MutableList<DataModel.Project> = ArrayList()

        for( i in data ){
            var project : DataModel.Project
            project = DataModel.Project(
                i.id,
                i.name,
                i.creationDate,
                i.visibility,
                group!!.groupCreator
            )
            projects.add(project)
        }
        return projects
    }

    private fun convertResponseToProjectModel(
        data: ResponseModel.ProjectResponse
    ): DataModel.Project{

        var project : DataModel.Project
        project = DataModel.Project(
            data.id,
            data.name,
            data.creationDate,
            data.visibility,
            group!!.groupCreator
        )

        return project
    }

    private fun copyRecyclerDataInProject(){
        projects = ArrayList()
        val data = recyclerAdapter.getData()
        for( i in data ){
            projects.add(i as DataModel.Project)
        }
    }

    private fun createProject(){
        val request : RequestModel.CreateProjectRequest = RequestModel.CreateProjectRequest(
            group_project_fragment_form_project_name.text.toString(),
            true,
            group!!.id!!
        )
        NetworkManager.createProject(request, userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.ProjectResponse> {
                override fun onFailure(call: Call<ResponseModel.ProjectResponse>, t: Throwable) {
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.ProjectResponse>,
                    response: Response<ResponseModel.ProjectResponse>
                ) {
                    if(response.code() != 200){
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "The project has been created", Toast.LENGTH_LONG).show()
                        copyRecyclerDataInProject()
                        projects.add(convertResponseToProjectModel(response.body()!!))
                        recyclerAdapter.setData(projects)
                        group_project_fragment_recycler_view.scrollToPosition(projects.size - 1)
                        group_project_fragment_form_project_name.text = Editable.Factory.getInstance().newEditable("")
                        group_project_fragment_form_project_close.performClick()
                        val inputMethodManager =
                            context?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                        // on below line hiding our keyboard.
                        inputMethodManager.hideSoftInputFromWindow(view!!.getWindowToken(), 0)
                    }
                }
            }
            )
    }
}
package com.mehdi.myapplication;

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.API.NetworkManager.createProject
import com.mehdi.myapplication.adapter.GroupTableAdapter
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.create_group_activity.*
import kotlinx.android.synthetic.main.group_activity.*
import kotlinx.android.synthetic.main.group_home_activity_header.*
import kotlinx.android.synthetic.main.group_members_fragment.*
import kotlinx.android.synthetic.main.group_project_fragment.*
import kotlinx.android.synthetic.main.message_fragment.*
import kotlinx.android.synthetic.main.profile_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class GroupActivity : AppCompatActivity() {

    private var idGroup : Long = -1
    private lateinit var group: DataModel.Group
    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private lateinit var userConnectedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_activity)

        loadProperties()
        loadGroup()

        group_back_arrow.setOnClickListener {
            finish()
        }

    }

    private fun loadProperties(){
        val intent = intent
        val extras = intent.extras
        idGroup =(extras ?: return).getLong("idGroup")
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
    }

    private fun loadGroup(){
        // todo test it
        NetworkManager.getGroupById(idGroup,userConnectedPreferences!!.getString("accessToken", null)!!
        )
            .enqueue(object: Callback<ResponseModel.GroupResponse> {
                override fun onFailure(call: Call<ResponseModel.GroupResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.GroupResponse>,
                    response: Response<ResponseModel.GroupResponse>
                ) {
                        print(response.body())
                        group = convertResponseToGroupModel(response.body()!!)
                        setTableLayout()
                        loadGroupProjects()

                }
            }
            )
    }

    private fun setTableLayout(){
        group_view_pager.adapter = GroupTableAdapter(this, group)
        TabLayoutMediator(group_tab_layout, group_view_pager){ tab: TabLayout.Tab, i: Int ->
            if (i == 0) {
                tab.text = "Projects"
            } else if (i == 1) {
                tab.text = "Members"
            }
        }.attach()
    }

    private fun loadGroupProjects(){
        // todo test it
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.ProjectResponse>
            try {
                response = NetworkManager.getProjectFromGroupByGroupId(group.id,userConnectedPreferences.getString("accessToken", null)!! )
                withContext(Dispatchers.Main) {
                    group.nbProject = response.size.toLong()
                    setDataOnLayout()
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    private fun convertResponseToGroupModel(response: ResponseModel.GroupResponse):
            DataModel.Group {
        // todo test it
        val groupCreator = response.creatorId
        var members: MutableList<DataModel.UserInGroup> = ArrayList()
        for (i in response.members){
            var member: DataModel.UserInGroup
            member = DataModel.UserInGroup(
                i.id,
                i.firstName + ' ' + i.lastName,
                i.imgUrl,
                i.city,
                groupCreator
            )
            members.add(member)
        }
        return DataModel.Group(
            response.id,
            response.name,
            members,
            -1,
            groupCreator
        )
    }

    private fun setDataOnLayout(){
        group_groupname.text = group.name
        group_home_nb_members.text = getString(R.string.nb_members, group.nbMembers.size.toString())
        group_home_nb_project.text = getString(R.string.nb_projects, group.nbProject.toString())
    }

    private fun createProject(){
        val request : RequestModel.CreateProjectRequest = RequestModel.CreateProjectRequest(
            create_group_groupname_value.text.toString(),
            true,
            group.id
        )
        NetworkManager.createProject(request, userConnectedPreferences.getString("accessToken", null)!!)
            .enqueue(object: Callback<ResponseModel.ProjectResponse> {
                override fun onFailure(call: Call<ResponseModel.ProjectResponse>, t: Throwable) {
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseModel.ProjectResponse>,
                    response: Response<ResponseModel.ProjectResponse>
                ) {
                    if(response.code() != 201){
                        Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(applicationContext, "The project has been created", Toast.LENGTH_LONG).show()
                        val response = response.body()
                        val resultIntent = Intent()
                        resultIntent.putExtra("groupCreated", Gson().toJson(response))
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
            )
    }
}

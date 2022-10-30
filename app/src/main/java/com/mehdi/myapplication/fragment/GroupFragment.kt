package com.mehdi.myapplication.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.AnswerActivity
import com.mehdi.myapplication.CreateGroupActivity
import com.mehdi.myapplication.R
import com.mehdi.myapplication.adapter.RecyclerAdapter
import com.mehdi.myapplication.models.DataModel
import com.mehdi.myapplication.models.RequestModel
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.group_fragment.*
import kotlinx.android.synthetic.main.message_fragment.*
import kotlinx.android.synthetic.main.post_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupFragment: Fragment() {

    private val recyclerAdapter: RecyclerAdapter = RecyclerAdapter()
    private lateinit var userConnectedPreferences : SharedPreferences
    private var groups: MutableList<DataModel.Group> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.group_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        loadProperties()
        loadGroups()
        setActions()
    }

    private fun setActions(){
        group_fragment_create_group.setOnClickListener {
            val intent = Intent(context, CreateGroupActivity::class.java)
            getResult.launch(intent)
        }
    }

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK){
                val result = it.data?.getStringExtra("groupCreated")
                val groupCreated = Gson().fromJson(result, ResponseModel.GroupResponse::class.java)
                addAnswerToAnswerList(convertResponseToGroupModel(groupCreated))
            }
        }

    private fun addAnswerToAnswerList(answer: DataModel.Group){
        groups.add(answer)
        recyclerAdapter.setData(groups)
        recycler_view_group.scrollToPosition(groups.size - 1)
    }

    private fun loadProperties(){
        userConnectedPreferences = this.activity?.getSharedPreferences("Login", Context.MODE_PRIVATE)!!
    }

    private fun loadGroups(){
        GlobalScope.launch(Dispatchers.Default) {
            val response: MutableList<ResponseModel.GroupResponse>
            try {
                response = NetworkManager.getGroupsByUserEmail(userConnectedPreferences.getString("email", null)!!, userConnectedPreferences.getString("accessToken", null)!! )
                withContext(Dispatchers.Main) {
                    groups = convertResponseToGroupModel(response)
                    recyclerAdapter.setData(groups)
                    recycler_view_group.adapter = recyclerAdapter
                }
            } catch (t: Throwable) {
                print(t)
            }
        }
    }

    private fun convertResponseToGroupModel(response: List<ResponseModel.GroupResponse>): MutableList<DataModel.Group> {
        var groups: MutableList<DataModel.Group> = ArrayList()

        for (i in response){
            val groupCreator = i.creatorId
            var group: DataModel.Group = DataModel.Group(
                i.id,
                i.name,
                convertMembersResponseToUserInGroupModel(i.members,groupCreator),
                -1,
                groupCreator
            )
            groups.add(group)
        }
        return groups
    }

    private fun convertResponseToGroupModel(response: ResponseModel.GroupResponse): DataModel.Group {

        var group: DataModel.Group = DataModel.Group(
            response.id,
            response.name,
            convertMembersResponseToUserInGroupModel(response.members, response.creatorId),
            -1,
            response.creatorId
        )
        return group
    }

    private fun convertMembersResponseToUserInGroupModel(memberResponse: List<ResponseModel.UserInfoResponse>, groupCreator: Long):
            MutableList<DataModel.UserInGroup> {
        var members: MutableList<DataModel.UserInGroup> = ArrayList()
        for (i in memberResponse){
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
        return members
    }
}
package com.mehdi.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.mehdi.myapplication.API.NetworkManager
import com.mehdi.myapplication.fragment.ChatFragment
import com.mehdi.myapplication.fragment.HomeFragment
import com.mehdi.myapplication.fragment.GroupFragment
import com.mehdi.myapplication.fragment.SearchFragment
import com.mehdi.myapplication.models.ResponseModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header_chat.*
import kotlinx.android.synthetic.main.header_search.*
import kotlinx.android.synthetic.main.left_menu.*
import kotlinx.android.synthetic.main.left_menu.view.*
import kotlinx.android.synthetic.main.profile_activity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var userConnectedPreferences : SharedPreferences
// TODO create an empty activity witch determine if login or not

//    override fun onStart() {
//        super.onStart()
//        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
//        if ( preference.getString("email", null) == null){
//            val intent = Intent(this, ConnectionActivity::class.java)
//            startActivity(intent)
//        }
//        setupLeftMenuData(preference)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadProperties()

        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        if ( preference.getString("accessToken", null) == null){
            val intent = Intent(this, ConnectionActivity::class.java)
            startActivity(intent)
            return
        }
        setContentView(R.layout.activity_main)
        setupLeftMenuData(preference)

//        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
//        if ( preference.getString("email", null) == null){
//            val intent = Intent(this, ConnectionActivity::class.java)
//            startActivity(intent)
//        }
//        setupLeftMenuData(preference)

        profil_icon_header_search.setOnClickListener{
            main_activity_drawer_layout.openDrawer(GravityCompat.START)
        }

//        var toast = Toast.makeText(applicationContext, preference.getString("email", null), Toast.LENGTH_SHORT)
//        toast.show()

        val searchFragment= SearchFragment()
        val chatFragment= ChatFragment()
        val homeFragment = HomeFragment()
        val groupFragment = GroupFragment()
//        profil_icon_header_search.setOnClickListener{
//            val intent = Intent(this, ProfileActivity::class.java)
//            //intent.putExtra("album", Gson().toJson(item))
//            startActivity(intent)
//        }

        //search_input.requestFocus()

//        search_site.seton { textView , actionId, event ->
//            if(actionId == EditorInfo.IME_ACTION_DONE){
//                var toast = Toast.makeText(applicationContext, "j'ai envoyé ", Toast.LENGTH_SHORT)
//                toast.show()
//                val inputManager =
//                    applicationContext.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//                inputManager.hideSoftInputFromWindow(
//                    this.currentFocus!!.windowToken,
//                    InputMethodManager.HIDE_NOT_ALWAYS
//                )
//                true
//            } else {
//                false
//            }
//        }

        setCurrentFragment(homeFragment)

        left_menu.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.left_menu_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("idUser", userConnectedPreferences!!.getLong("idUser", -1))
                    startActivity(intent)
                }
                R.id.left_menu_subscription -> {
                    val intent = Intent(this, SubscriptionActivity::class.java)
                    intent.putExtra("idUser", userConnectedPreferences!!.getLong("idUser", -1))
                    startActivity(intent)
                }
                R.id.left_menu_log_out -> {
                    cleanSharedPreference()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
            true
        }

        bottom_navigation.setOnItemSelectedListener {
            when(it.itemId){
                R.id.homeFragmentItem->setCurrentFragment(homeFragment)
                R.id.searchFragmentItem->setCurrentFragment(searchFragment)
                R.id.notificationFragmentItem->setCurrentFragment(groupFragment)
                R.id.messageFragmentItem->setCurrentFragment(chatFragment)
            }
            true
        }

//        search_bar.setOnClickListener {
//            search_bar_placeholder.text = "ca marche !!!"
//        }
    }

    private fun loadProperties(){
        userConnectedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE)
    }

    private fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun setClickAvatar(){
        profil_icon_header_chat.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            //intent.putExtra("album", Gson().toJson(item))
            startActivity(intent)
        }
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            setHeader(fragment)
            commit()
        }

    private fun setHeader(fragment: Fragment) {
        when ( fragment){
            is ChatFragment -> fragment_title.text = "CHAT"
            is SearchFragment -> fragment_title.text = "SEARCH"
            is HomeFragment -> fragment_title.text = "HOME"
            is GroupFragment -> fragment_title.text = "GROUP"
        }
    }

    private fun setupLeftMenuData(preferences: SharedPreferences){

        val image = preferences.getString("imageUrl", null)
        val imageByte = image?.toByteArray()
        val options = BitmapFactory.Options()
        val bitmap = imageByte?.let { BitmapFactory.decodeByteArray(imageByte, 0, it.size) }
        val headerView: View = left_menu.getHeaderView(0)
        headerView.user_pseudo.text = preferences.getString("firstname", null) + " " + preferences.getString("lastname", null)
        headerView.cv_avatar_image.setImageBitmap(bitmap)

        GlobalScope.launch {
            val getUserProfilImage: Call<ResponseModel.ImageResponse> =
                NetworkManager.getUserProfilImage(
                    preferences.getLong("idUser", -1),
                    userConnectedPreferences!!.getString("accessToken", null)!!
                )
            var userProfilImage = getUserProfilImage.execute().body()
            var imageToBitmap: Bitmap? = null
            if (userProfilImage != null) {
                if ( userProfilImage.file != null) {
                    val imageToByte = Base64.getDecoder().decode(userProfilImage!!.file)
                    imageToBitmap =
                        BitmapFactory.decodeByteArray(imageToByte, 0, imageToByte.size)
                    print(userProfilImage)
                }
            }
            withContext(Dispatchers.Main) {
                if (imageToBitmap != null) {
                    //cv_avatar_image.setImageBitmap(imageToBitmap)
                    header_search_avatar.setImageBitmap(imageToBitmap)
                }

            }
        }
    }

    private fun cleanSharedPreference(){
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
        val preference : SharedPreferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        val Ed = preference.edit()
        Ed.remove("accessToken")
        Ed.commit()
    }

}



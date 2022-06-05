package yash_g.ritik_k.YR_messenger

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import yash_g.ritik_k.YR_messenger.adapters.TopStatusAdapter
import yash_g.ritik_k.YR_messenger.adapters.UsersAdapter
import yash_g.ritik_k.YR_messenger.databinding.ActivityMainBinding
import yash_g.ritik_k.YR_messenger.model.Status
import yash_g.ritik_k.YR_messenger.model.User
import yash_g.ritik_k.YR_messenger.model.UserStatus
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var DBref: FirebaseFirestore
    private lateinit var usersArray:ArrayList<User>
    private lateinit var statusArray:ArrayList<UserStatus>
    private lateinit var usersAdapter:UsersAdapter
    private lateinit var TopStatusAdapter:TopStatusAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog
    private lateinit var user:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val configSettings: FirebaseRemoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0)
            .build()
        val mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.fetchAndActivate().addOnSuccessListener {
            val toolbarColor:String =mFirebaseRemoteConfig.getString("toolbarColor")
            val toolbarImage =mFirebaseRemoteConfig.getString("toolbarImage")
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor(toolbarColor)))

        }



        DBref = FirebaseFirestore.getInstance()
        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            val tokenMap:HashMap<String,Any> = HashMap()
            tokenMap.put("token",it)
            DBref.collection("users").document(Firebase.auth.uid!!).set(tokenMap,
                SetOptions.merge())
        }

        usersArray = ArrayList()
        statusArray = ArrayList()

        binding.recyclerView.showShimmerAdapter()

        DBref.collection("users").document(Firebase.auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                user  = it.toObject(User::class.java)!!
            }

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("uploading image")
        progressDialog.setCancelable(false)

        usersAdapter = UsersAdapter(mContext = this,mUsers=usersArray)
        TopStatusAdapter = TopStatusAdapter(mContext = this,statusList = statusArray)
        val statusListLayoutManager=LinearLayoutManager(this)
        statusListLayoutManager.setOrientation(RecyclerView.HORIZONTAL)
        binding.recyclerView.adapter= usersAdapter
        binding.statusList.apply {
            adapter = TopStatusAdapter
            layoutManager = statusListLayoutManager
        }

        binding.bottomNavigationView.setOnNavigationItemSelectedListener {
             when(it.itemId){
                 R.id.status-> {
                     val intent: Intent = Intent()
                     intent.setType("image/*")
                     intent.setAction(Intent.ACTION_GET_CONTENT)
                     startActivityForResult(intent,75)
                     return@setOnNavigationItemSelectedListener true
                 }
                R.id.calls->{

                    return@setOnNavigationItemSelectedListener true
                }
                R.id.chats->{
                    val intent: Intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    return@setOnNavigationItemSelectedListener true
                }
                else->throw AssertionError()
             }
        }

        DBref.collection("users").get().addOnSuccessListener {
              (usersArray as ArrayList<User>).clear()
              for(snapshot in it){
                    val user:User =snapshot.toObject(User::class.java)
                    if(!(user.uid.equals(Firebase.auth.currentUser!!.uid)))
                        (usersArray as ArrayList<User>).add(user!!)
              }
            binding.recyclerView.hideShimmerAdapter()
              usersAdapter.notifyDataSetChanged()
        }


        DBref.collection("stories").addSnapshotListener {snapshot, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && !(snapshot.documents.isEmpty())) {
                statusArray.clear()
                for(snap in snapshot.documents){
                    val uStatus:UserStatus = UserStatus()
                    uStatus.apply {
                        setname(snap!!.get("name").toString())
                        setlastupdated(snap!!.getLong("lastupdated")!!)
                        setprofileimage(snap!!.get("profileImage").toString())
                    }
                    val statuesArray=ArrayList<Status>()
                    DBref.collection("stories").document(snap.id).collection("statues")
                        .addSnapshotListener { value, error ->
                            statuesArray.clear()
                            for (status in value!!.documents){
                                val tempStatus = status!!.toObject(Status::class.java)!!
                                statuesArray.add(tempStatus)
                                Log.e("essssssssss","${statuesArray.size}")
                            }
                            uStatus.setstatusList(statuesArray!!)
                            Log.e("essssssssss","size:${uStatus.getstatusList().size}")
                        }
                    statusArray.add(uStatus)

                }

            } else {
                Log.d("TAG", "Current data: null")
            }
            TopStatusAdapter.notifyDataSetChanged()
        }

    }

    override fun onResume() {
        val userOnlinestatus:HashMap<String,Any> = HashMap()
        userOnlinestatus.put("status","online")
        DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus, SetOptions.merge())
        super.onResume()
    }
    override fun onPause() {
        super.onPause()
        val userOnlinestatus:HashMap<String,Any> = HashMap()
        userOnlinestatus.put("status","offline")
        DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus, SetOptions.merge())
    }
    override fun onStop() {
        val userOnlinestatus:HashMap<String,Any> = HashMap()
        userOnlinestatus.put("status","offline")
        DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus, SetOptions.merge())
        super.onStop()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(75==requestCode && resultCode == Activity.RESULT_OK && data!=null){
            if(data.data!=null){
                progressDialog.show()
                storage= FirebaseStorage.getInstance()
                val storageRef =storage.getReference().child("status")
                    .child(Date().time.toString()+"")
                storageRef.putFile(data.data!!).addOnCompleteListener{
                    storageRef.downloadUrl.addOnSuccessListener {
                        val userStatusObj=UserStatus()
                        userStatusObj.setname(user.name)
                        userStatusObj.setprofileimage(user.profileImage)
                        userStatusObj.setlastupdated(Date().time)
                        val userStatusMap:HashMap<String,Any?> = HashMap()
                        userStatusMap.put("name",userStatusObj.getname())
                        userStatusMap.put("profileImage",userStatusObj.getprofileimage())
                        userStatusMap.put("lastupdated",userStatusObj.getlastupdated())
                        val statusObj =Status(it.toString(),userStatusObj.getlastupdated())
                        DBref.collection("stories")
                            .document(Firebase.auth.uid!!).set(userStatusMap, SetOptions.merge())
                        DBref.collection("stories")
                            .document(Firebase.auth.uid!!).collection("statues")
                            .document().set(statusObj)
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu:Menu):Boolean{
        menuInflater.inflate(R.menu.topmenu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.search->
                Toast.makeText(this,"search clicked",Toast.LENGTH_SHORT).show()
            R.id.group-> {
                /*startActivity(Intent(this, GroupChatActivity::class.java))
                */
                startActivity(Intent(this, NewGroupChatActivity::class.java))


                Toast.makeText(this, "group clicked", Toast.LENGTH_SHORT).show()
            }R.id.group_create->
            startActivity(Intent(this, CreateGroupActivity::class.java))
            R.id.settings->
                Toast.makeText(this,"settings clicked",Toast.LENGTH_SHORT).show()

        }
        return super.onOptionsItemSelected(item)
    }
}
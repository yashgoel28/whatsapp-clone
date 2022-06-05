package yash_g.ritik_k.YR_messenger

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject
import yash_g.ritik_k.YR_messenger.adapters.MessagesAdapter
import yash_g.ritik_k.YR_messenger.databinding.ActivityChatBinding
import yash_g.ritik_k.YR_messenger.model.Message
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var messagesArray: ArrayList<Message>
    private lateinit var SenderRoom: String
    private lateinit var ReceiverRoom: String
    private lateinit var DBref: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog
    private lateinit var receiverUid:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name: String? = intent.getStringExtra("name")
        val profile: String? = intent.getStringExtra("image")
        receiverUid = intent.getStringExtra("uid")!!
        val recieverDeviceToken: String? = intent.getStringExtra("token")
        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.name.setText(name)
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.avatar).into(binding.profile)

        binding.imageView2.setOnClickListener {
            finish()
        }

        SenderRoom = Firebase.auth.uid!! + receiverUid
        ReceiverRoom = receiverUid + Firebase.auth.uid!!

        messagesArray = ArrayList()
        messagesArray.sortBy {
            it.gettimestamp()
        }
        adapter = MessagesAdapter(this, messages = messagesArray,senderRoom = SenderRoom,receiverRoom=ReceiverRoom)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        DBref = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Image...")
        progressDialog.setCancelable(false)

        val handler :Handler= Handler()

        DBref.collection("presence").document(receiverUid!!).addSnapshotListener { value, error ->
            if(value!=null && value.exists()){
                val status = value.get("status").toString()
                binding.status.setText(status)
                binding.status.isVisible =true
                if(status.equals("offline"))
                    binding.status.isVisible =false
            }
        }

        DBref.collection("chats").document(SenderRoom)
            .collection("messages").get()
            .addOnSuccessListener {
                messagesArray.clear()
                for (document in it) {
                    val message: Message = document.toObject(Message::class.java)
                    messagesArray.add(message!!)
                    messagesArray.sortBy {
                        it.gettimestamp()
                    }
                }
                adapter.notifyDataSetChanged()
                binding.recyclerView.scrollToPosition(binding.recyclerView.adapter!!.itemCount -1)

            }

        listenForMessages()
        binding.sendBtn.setOnClickListener {
            val msgTxt: String = binding.messageBox.text.toString()
            val message: Message = Message(msgTxt, Firebase.auth.uid!!, Date().time)
            val randomKey = DBref.collection("randomkeys").document().id

            val lastMsgMap:HashMap<String,Any> = HashMap()
            lastMsgMap.put("lastMsg",message.getmessage())
            lastMsgMap.put("lastMsgTime",Date().time)
            DBref.collection("chats").document(SenderRoom).collection("lastMsgs")
                .document("lastmsg").set(lastMsgMap,SetOptions.merge())
            DBref.collection("chats").document(ReceiverRoom).collection("lastMsgs")
                .document("lastmsg").set(lastMsgMap,SetOptions.merge())

            message.setmessageId(randomKey)
            binding.messageBox.setText("")
            DBref.collection("chats").document(SenderRoom)
                .collection("messages").document(randomKey).set(message)
                .addOnSuccessListener {
                }
            DBref.collection("chats").document(ReceiverRoom)
                .collection("messages").document(randomKey).set(message).addOnSuccessListener {
                    sendNotifications(name!!, message.getmessage(),recieverDeviceToken!!)
                }
        }

        binding.attachment.setOnClickListener {
            val intent=Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent,25)
        }




        binding.messageBox.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                val userOnlinestatus:HashMap<String,Any> = HashMap()
                userOnlinestatus.put("status","typing....")
                DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus,
                    SetOptions.merge())
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping,1500)
            }
            var userStoppedTyping=Runnable{
                val userOnlinestatus:HashMap<String,Any> = HashMap()
                userOnlinestatus.put("status","online")
                DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus)
            }
        })
    }//end of onCreate

    override fun onResume() {
        val userOnlinestatus:HashMap<String,Any> = HashMap()
        userOnlinestatus.put("status","online")
        DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus, SetOptions.merge())
        super.onResume()
    }

    override fun onStop() {
        val userOnlinestatus:HashMap<String,Any> = HashMap()
        userOnlinestatus.put("status","offline")
        DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus, SetOptions.merge())
        super.onStop()
    }
    override fun onPause() {
        super.onPause()
        val userOnlinestatus:HashMap<String,Any> = HashMap()
        userOnlinestatus.put("status","offline")
        DBref.collection("presence").document(Firebase.auth.uid!!).set(userOnlinestatus, SetOptions.merge())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 25 && data!=null && data.data!=null && resultCode == RESULT_OK){
            progressDialog.show()
            var selectedImage:Uri =  data.data!!
            val storageRef = storage.getReference().child("chats").child(Calendar.getInstance().timeInMillis.toString())
            storageRef.putFile(selectedImage).addOnCompleteListener{
                if(it.isSuccessful){
                    progressDialog.dismiss()
                    storageRef.downloadUrl.addOnSuccessListener {
                        val filePath:String =it.toString()
                        val msgTxt: String = binding.messageBox.text.toString()
                        val message: Message = Message(msgTxt, Firebase.auth.uid!!, Date().time)
                        val randomKey = DBref.collection("randomkeys").document().id

                        val lastMsgMap:HashMap<String,Any> = HashMap()
                        lastMsgMap.put("lastMsg",message.getmessage())
                        lastMsgMap.put("lastMsgTime",Date().time)
                        DBref.collection("chats").document(SenderRoom).collection("lastMsgs")
                            .document("lastmsg").set(lastMsgMap,SetOptions.merge())
                        DBref.collection("chats").document(ReceiverRoom).collection("lastMsgs")
                            .document("lastmsg").set(lastMsgMap,SetOptions.merge())

                        message.setmessageId(randomKey)
                        message.setimageUrl(filePath)
                        message.setmessage("asbvQQdsdDDFDs5445888521")
                        binding.messageBox.setText("")
                        DBref.collection("chats").document(SenderRoom)
                            .collection("messages").document(randomKey).set(message)
                            .addOnSuccessListener { }
                        DBref.collection("chats").document(ReceiverRoom)
                            .collection("messages").document(randomKey).set(message)
                    }
                }
            }
        }
    }


    private fun listenForMessages() {
        val DBref = FirebaseFirestore.getInstance().collection("chats")
            .document(SenderRoom).collection("messages")
            .addSnapshotListener { querySnapshot, error ->
                error?.let {
                    Toast.makeText(this,"asdasds",Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                querySnapshot?.let{
                    messagesArray.clear()
                    for(document in it.documents){
                        val message: Message? = document.toObject(Message::class.java)
                        messagesArray.add(message!!)
                        messagesArray.sortBy {
                            it.gettimestamp()
                        }
                        Log.e("error ddd","${document.data}")
                        adapter.notifyDataSetChanged()
                        binding.recyclerView.scrollToPosition(binding.recyclerView.adapter!!.itemCount -1)
                    }
                }

        }
        FirebaseFirestore.getInstance().collection("presence").document(receiverUid!!).addSnapshotListener { value, error ->
            if(value!=null && value.exists()){
                val status = value.get("status").toString()
                binding.status.setText(status)
                binding.status.isVisible =true
                if(status.equals("offline"))
                    binding.status.isVisible =false
            }
        }

    }

    fun sendNotifications(senderName:String,message:String,token:String){
        val queue:RequestQueue =Volley.newRequestQueue(this)
        val url:String ="https://fcm.googleapis.com/fcm/send"
        val data :JSONObject = JSONObject()
        data.apply {
            put("title",senderName)
            put("body",message)
        }
        val notificationData:JSONObject = JSONObject()
        notificationData.apply {
            put("notification",data)
            put("to",token)
        }
        val request:JsonObjectRequest = object:JsonObjectRequest(Request.Method.POST,url,notificationData,
            Response.Listener {
            Toast.makeText(this,"notification Sent",Toast.LENGTH_SHORT).show()
        },
        Response.ErrorListener {
            Toast.makeText(this,"Error ${it}",Toast.LENGTH_SHORT).show()
        }){
            @Throws(AuthFailureError::class)
            override fun getHeaders():Map<String,String>{
                val map = HashMap<String, String>()
                val key ="key=AAAADkkUPiA:APA91bERxKs_4-rsY9L44CC9cBBrZ92lOSDt5YgnrZr-eA2FA-NKqide1CZ5lFLwMEFB_kZ24OLTtwbxp3BcA6bnoB030AQuah9q-LQSaK-vzQvCUpR7wchsRsRmJCepgx7aVxETfQsd"
                map["Content-Type"] = "application/json"
                map["Authorization"] = key
                return map
            }
        }
        queue.add(request)

    }
}


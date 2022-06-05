package yash_g.ritik_k.YR_messenger

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import yash_g.ritik_k.YR_messenger.adapters.GroupMessagesAdapter
import yash_g.ritik_k.YR_messenger.adapters.MessagesAdapter
import yash_g.ritik_k.YR_messenger.databinding.ActivityGroupChatBinding
import yash_g.ritik_k.YR_messenger.model.Message
import java.util.*
import kotlin.collections.ArrayList

class GroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGroupChatBinding
    private lateinit var adapter: GroupMessagesAdapter
    private lateinit var messagesArray: ArrayList<Message>
    private lateinit var DBref: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding!!.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        DBref = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        messagesArray = ArrayList()
        messagesArray.sortBy {
            it.gettimestamp()
        }

        adapter = GroupMessagesAdapter(this, messages = messagesArray)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter


        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading Image...")
        progressDialog.setCancelable(false)

        binding.imageView2.setOnClickListener {
            finish()
        }

        binding.sendBtn.setOnClickListener {
            val msgTxt: String = binding.messageBox.text.toString()
            val message: Message = Message(msgTxt, Firebase.auth.uid!!, Date().time)
            binding.messageBox.setText("")
            DBref.collection("groupchats").document().set(message)
        }
        DBref.collection("groupchats").get()
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

    }//end of onCreate

    private fun listenForMessages() {
        val DBref = FirebaseFirestore.getInstance().collection("groupchats")
            .addSnapshotListener { querySnapshot, error ->
                error?.let {
                    Toast.makeText(this,"asdasds", Toast.LENGTH_LONG).show()
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
    }
}
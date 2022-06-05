package yash_g.ritik_k.YR_messenger.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import yash_g.ritik_k.YR_messenger.ChatActivity
import yash_g.ritik_k.YR_messenger.R
import yash_g.ritik_k.YR_messenger.databinding.RowConversationBinding
import yash_g.ritik_k.YR_messenger.model.User
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class UsersAdapter(mContext:Context,mUsers:ArrayList<User>):RecyclerView.Adapter<UsersAdapter.ViewHolder?>(){
    private val mContext: Context
    private val mUsers:List<User>
    private lateinit var binding:RowConversationBinding

    init {
        this.mContext =mContext
        this.mUsers =mUsers
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        var usernametxt :TextView
        var profileimg :CircleImageView
        var lastmsgtxt :TextView
        var binding:RowConversationBinding
        init {
            usernametxt = itemView.findViewById(R.id.username)
            profileimg = itemView.findViewById(R.id.profile)
            lastmsgtxt = itemView.findViewById(R.id.lastMsg)
            binding= RowConversationBinding.bind(itemView)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersAdapter.ViewHolder {
        val view: View = LayoutInflater.from(mContext).inflate(R.layout.row_conversation, parent, false)
        return UsersAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersAdapter.ViewHolder, position: Int) {
        val user:User = mUsers[position]
        val SenderRoom = Firebase.auth!!.uid + user.uid
        FirebaseFirestore.getInstance().collection("chats")
            .document(SenderRoom).collection("lastMsgs")
            .addSnapshotListener { querySnapshot, error ->
                val dateFormat: SimpleDateFormat = SimpleDateFormat("hh::mm a")

                if(querySnapshot!=null && querySnapshot.documents.size>0) {
                    Log.e("lastmsgs","${querySnapshot!!.documents[0].data}")
                    holder.binding.lastMsg.text = (querySnapshot!!.documents[0].data!!.get("lastMsg")).toString()
                    val time = dateFormat.format((querySnapshot!!.documents[0].data!!.get("lastMsgTime")))
                    holder.binding.msgTime.text = time.toString()
                    Log.e("errd","${querySnapshot!!.documents[querySnapshot.documents.size-1].data}")
                }else{
                    holder.binding.lastMsg.text ="Tap To Chat"
                }
       }

        holder.usernametxt.text = user!!.name
        if(user!!.profileImage.isNotEmpty())
            Picasso.get().load(user!!.profileImage).placeholder(R.drawable.avatar).into(holder.profileimg)

        holder.itemView.setOnClickListener {
            val intent= Intent(mContext, ChatActivity::class.java)
                .putExtra("name","${user.name}")
                .putExtra("uid","${user.uid}")
                .putExtra("image","${user.profileImage}")
                .putExtra("token","${user.token}")
            mContext.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

}
package yash_g.ritik_k.YR_messenger.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.pgreze.reactions.*
import com.github.pgreze.reactions.dsl.reactionConfig
import com.github.pgreze.reactions.dsl.reactionPopup
import com.github.pgreze.reactions.dsl.reactions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import yash_g.ritik_k.YR_messenger.R
import yash_g.ritik_k.YR_messenger.databinding.ItemSentBinding
import yash_g.ritik_k.YR_messenger.databinding.ItemReceiveBinding
import yash_g.ritik_k.YR_messenger.databinding.DeleteDialogBinding
import yash_g.ritik_k.YR_messenger.model.Message

class MessagesAdapter(context: Context, messages: ArrayList<Message>,senderRoom:String,receiverRoom:String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val context: Context
    private val messages: ArrayList<Message>
    private val ITEM_SENT = 1
    private val ITEM_RECEIVE = 2
    private var senderRoom:String=""
    private var receiverRoom:String=""
    init {
        this.context =context
        this.messages = messages
        this.senderRoom =senderRoom
        this.receiverRoom=receiverRoom

    }

    class Sent_ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val SentBinding = ItemSentBinding.bind(itemView)
    }
    class Receive_ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val RecBinding = ItemReceiveBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == ITEM_SENT){
            val view:View = LayoutInflater.from(context).inflate(R.layout.item_sent,parent,false)
            return Sent_ViewHolder(view)
        }else{
            val view:View = LayoutInflater.from(context).inflate(R.layout.item_receive,parent,false)
            return Receive_ViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message:Message = messages.get(position)
        if(Firebase.auth.uid!!.equals(message.getsenderId())){
            return ITEM_SENT
        }else{
            return ITEM_RECEIVE
        }
        return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        val reactions =intArrayOf(
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
        )
        val config: ReactionsConfig = ReactionsConfigBuilder(context)
            .withReactions(reactions)
            .build()
        val popup = ReactionPopup(
            context,
            config,
            object : ReactionSelectedListener {
                override fun invoke(pos: Int): Boolean = true.also {
                    if(holder.javaClass == Sent_ViewHolder::class.java) {
                        val viewHolder = holder as Sent_ViewHolder
                        if (pos >= 0) {
                            viewHolder.SentBinding.feeling.setImageResource(reactions[pos])
                            viewHolder.SentBinding.feeling.isVisible = true
                        }
                    }
                    else{
                        val viewHolder = holder as Receive_ViewHolder
                        if (pos>=0){
                            viewHolder.RecBinding.feeling.setImageResource(reactions[pos])
                            viewHolder.RecBinding.feeling.isVisible =true
                        }
                    }
                    message.setfeeling(pos)
                    FirebaseFirestore.getInstance().collection("chats").document(senderRoom)
                        .collection("messages").document(message.getmessageId()).set(message)

                    FirebaseFirestore.getInstance().collection("chats").document(receiverRoom)
                        .collection("messages").document(message.getmessageId()).set(message)


                }
            }
        )

        if(holder.javaClass == Sent_ViewHolder::class.java){
            val viewHolder = holder as Sent_ViewHolder
            if(message.getmessage().equals("asbvQQdsdDDFDs5445888521")){
                viewHolder.SentBinding.image.setVisibility(View.VISIBLE)
                viewHolder.SentBinding.message.setVisibility(View.GONE)
                Glide.with(context).load(message.getimageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.SentBinding.image)
            }
            viewHolder.SentBinding.message.setText(message.getmessage())
            if(message.getfeeling() >=0){
                viewHolder.SentBinding.feeling.setImageResource(reactions[message.getfeeling() as Int])
                viewHolder.SentBinding.feeling.isVisible =true
            }else
                viewHolder.SentBinding.feeling.isVisible =false
            viewHolder.SentBinding.message.setOnTouchListener { view, motionEvent ->
                popup.onTouch(v=view,event = motionEvent)
            }
            viewHolder.itemView.setOnLongClickListener {
                val view: View = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
                val binding: DeleteDialogBinding = DeleteDialogBinding.bind(view)
                val dialog: AlertDialog= AlertDialog.Builder(context)
                    .setTitle("delete message")
                    .setView(binding.getRoot())
                    .create()
                binding.everyone.setOnClickListener {
                    message.setmessage("This message is removed.")
                    message.setfeeling(-1)
                    FirebaseFirestore.getInstance().collection("chats").document(senderRoom)
                        .collection("messages").document(message.getmessageId()).set(message)
                    FirebaseFirestore.getInstance().collection("chats").document(receiverRoom)
                        .collection("messages").document(message.getmessageId()).set(message)
                    dialog.dismiss()

                }
                binding.delete.setOnClickListener {
                    FirebaseFirestore.getInstance().collection("chats").document(senderRoom)
                        .collection("messages").document(message.getmessageId()).delete()
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                return@setOnLongClickListener false
            }

        }else{
            val viewHolder = holder as Receive_ViewHolder
            if(message.getmessage().equals("asbvQQdsdDDFDs5445888521")){
                viewHolder.RecBinding.image.setVisibility(View.VISIBLE)
                viewHolder.RecBinding.message.setVisibility(View.GONE)
                Glide.with(context).load(message.getimageUrl()).placeholder(R.drawable.placeholder).into(viewHolder.RecBinding.image)
            }
            viewHolder.RecBinding.message.setText(message.getmessage())
            if(message.getfeeling() >=0){
                viewHolder.RecBinding.feeling.setImageResource(reactions[message.getfeeling() as Int])
                viewHolder.RecBinding.feeling.isVisible =true
            }else
                viewHolder.RecBinding.feeling.isVisible =false
            viewHolder.RecBinding.message.setOnTouchListener { view, motionEvent ->
                popup.onTouch(v=view,event = motionEvent)
            }

            viewHolder.itemView.setOnLongClickListener {
                val view: View = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
                val binding: DeleteDialogBinding = DeleteDialogBinding.bind(view)
                val dialog: AlertDialog= AlertDialog.Builder(context)
                    .setTitle("delete message")
                    .setView(binding.getRoot())
                    .create()
                binding.everyone.isVisible =false
                binding.delete.setOnClickListener {
                    FirebaseFirestore.getInstance().collection("chats").document(senderRoom)
                        .collection("messages").document(message.getmessageId()).delete()
                    dialog.dismiss()
                }
                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                return@setOnLongClickListener false
            }

        }
    }


}
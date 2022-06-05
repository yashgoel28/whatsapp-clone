package yash_g.ritik_k.YR_messenger.adapters

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import yash_g.ritik_k.YR_messenger.databinding.ItemStatusBinding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import yash_g.ritik_k.YR_messenger.MainActivity
import yash_g.ritik_k.YR_messenger.R
import yash_g.ritik_k.YR_messenger.model.Status
import yash_g.ritik_k.YR_messenger.model.UserStatus
import java.util.stream.IntStream.range

class TopStatusAdapter(mContext:Context,statusList:ArrayList<UserStatus>): RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder>() {
    private val mContext:Context
    private val statusList:ArrayList<UserStatus>
    init {
        this.mContext =mContext
        this.statusList=statusList
    }

    class TopStatusViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val binding =ItemStatusBinding.bind(itemView)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopStatusViewHolder {
        val view =LayoutInflater.from(mContext).inflate(R.layout.item_status,parent,false)
        return TopStatusViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: TopStatusViewHolder, position: Int) {

        val userStatus:UserStatus= statusList[position]

        holder.binding.circularStatusView.setOnClickListener {
            val myStories :ArrayList<MyStory> = ArrayList()
            for (status in userStatus.getstatusList()){
                myStories.add(MyStory(status.getImageUrl()))
            }
            StoryView.Builder((mContext as MainActivity).supportFragmentManager)
                .setStoriesList(myStories)
                .setStoryDuration(5000)
                .setTitleText(userStatus.getname())
                .setSubtitleText("")
                .setTitleLogoUrl(userStatus.getprofileimage())
                .setStoryClickListeners(object: StoryClickListeners{
                    override fun onDescriptionClickListener(position: Int) {
                        TODO("Not yet implemented")
                    }

                    override fun onTitleIconClickListener(position: Int) {
                        TODO("Not yet implemented")
                    }
                }).build()
                .show()
        }

    }

    override fun getItemCount(): Int {
        return statusList.size
    }
}
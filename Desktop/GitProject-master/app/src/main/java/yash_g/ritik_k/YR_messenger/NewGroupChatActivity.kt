package yash_g.ritik_k.YR_messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import yash_g.ritik_k.YR_messenger.databinding.ActivityNewGroupChatBinding

class NewGroupChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewGroupChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
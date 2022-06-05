package yash_g.ritik_k.YR_messenger

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import yash_g.ritik_k.YR_messenger.databinding.ActivityPhoneNumberBinding


class PhoneNumberActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPhoneNumberBinding
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
    if(FirebaseAuth.getInstance().currentUser!=null){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }


        supportActionBar!!.hide()
        binding.phoneBox.requestFocus()

        binding.continueBtn.setOnClickListener{
            val intent = Intent(this,OtpActivity::class.java)
                .putExtra("phoneNumber",binding.phoneBox.text.toString())
                startActivity(intent)
        }
    }
}
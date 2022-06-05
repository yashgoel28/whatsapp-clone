package yash_g.ritik_k.YR_messenger

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import yash_g.ritik_k.YR_messenger.databinding.ActivityCreateGroupBinding
import yash_g.ritik_k.YR_messenger.databinding.ActivitySetUpProfileBinding
import yash_g.ritik_k.YR_messenger.model.User
import java.util.*
import kotlin.collections.HashMap


class CreateGroupActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateGroupBinding
    private val RESULT_CODE =22
    private lateinit var auth: FirebaseAuth
    private lateinit var storage:FirebaseStorage
    private lateinit var DBref: FirebaseFirestore
    private lateinit var selectedImg: Uri
    private lateinit var dialog: ProgressDialog
    private var imgSelectedFlag= false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        DBref = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = Firebase.auth

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Creating Group...")
        dialog!!.setCancelable(false)
        imgSelectedFlag = false

        binding.imageView.setOnClickListener {
            val intent= Intent()
            intent.apply {
                action= Intent.ACTION_GET_CONTENT
                type="image/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg","image/png"))

                startActivityForResult(intent,RESULT_CODE)
            }
        }
        binding.continueBtn.setOnClickListener{
            val name:String= binding.nameBox.text.toString().trim()
            val desc = binding.descriptionTextView.text.toString().trim()
            if(name.isNullOrBlank()){
                binding.nameBox.setError("please Enter Group name")
                return@setOnClickListener
            }
            if(desc.isNullOrBlank()){
            binding.nameBox.setError("please Enter Group description")
            return@setOnClickListener
            }
            dialog!!.show()
            if(imgSelectedFlag == true){
                val storageRef: StorageReference = storage.getReference().child("GroupProfiles").child(auth.uid!!)
                storageRef.putFile(selectedImg).addOnCompleteListener{task->
                    if(task.isSuccessful){
                        storageRef.downloadUrl.addOnSuccessListener {  uri->
                            val imageUrl:String=uri.toString()
                            creategroup(Calendar.getInstance().timeInMillis.toString(),name,desc,imageUrl)

                        }
                    }
                }
            }else{
                creategroup(Calendar.getInstance().timeInMillis.toString(),name,desc,"No Image")
            }
        }
    }

    fun creategroup(g_timestmp:String, grpTitle:String,grpDesc:String,gIcon:String){
        val grpHashMap:HashMap<String,String> = HashMap()
        grpHashMap.put("GID",g_timestmp)
        grpHashMap.put("GIcon",gIcon)
        grpHashMap.put("GTimeStamp",g_timestmp)
        grpHashMap.put("CreatedBy",FirebaseAuth.getInstance().uid!!)
        grpHashMap.put("GTitle",grpTitle)
        grpHashMap.put("GDesc",grpDesc)

        FirebaseFirestore.getInstance().collection("Groups").document("${g_timestmp}").set(grpHashMap).addOnSuccessListener {
            val participantHashMap:HashMap<String,String> = HashMap()
            participantHashMap.put("uid",FirebaseAuth.getInstance().uid!!)
            participantHashMap.put("role","creator")
            participantHashMap.put("timeStamp",g_timestmp)
            FirebaseFirestore.getInstance().collection("Groups").document("${g_timestmp}")
                .collection("participants").document(FirebaseAuth.getInstance().uid!!).set(g_timestmp)
        }
        dialog.dismiss()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(RESULT_CODE==requestCode && resultCode == Activity.RESULT_OK && data!=null){
            if(data.data!=null){
                binding.imageView.setImageURI(data.data!!)
                imgSelectedFlag = true
                selectedImg =data.data!!
            }
        }
    }
}

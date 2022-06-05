package yash_g.ritik_k.YR_messenger.model

import android.util.Log

class UserStatus {
    var uid: String = ""
    var name: String = ""
    var statusList:ArrayList<Status> = ArrayList()
    var profileImage: String = ""
    var lastUpdated: Long=0

    constructor(){}
    constructor(uid:String,name:String,statusList: ArrayList<Status>,
        profileImage:String,lastUpdated:Long){
        this.uid = uid
        this.name = name
        this.statusList = statusList
        this.profileImage = profileImage
        this.lastUpdated = lastUpdated

    }
    fun getname():String{return this.name}
    fun getuid():String{return this.uid}
    fun getlastupdated():Long{return this.lastUpdated}
    fun getstatusList():ArrayList<Status>{return this.statusList}
    fun getprofileimage():String{return this.profileImage}
    fun setname(name:String) {this.name=name}
    fun setuid(uid: String) {this.uid=uid}
    fun setlastupdated(lastUpdated: Long) {this.lastUpdated=lastUpdated }
    fun setstatusList(statusList: ArrayList<Status>) {this.statusList=statusList}
    fun setprofileimage(profileImage: String) {this.profileImage=profileImage}
}
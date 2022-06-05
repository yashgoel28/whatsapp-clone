package yash_g.ritik_k.YR_messenger.model;

class GroupChatList {
    var GID: String = ""
    var GIcon: String = ""
    var GTimeStamp: String  = ""
    var CreatedBy: String = ""
    var GTitle: String = ""
    var GDesc:String = ""
    constructor(){}
    constructor(GID: String,GIcon: String ,GTimeStamp: String,CreatedBy: String
        ,GTitle: String,GDesc:String){
        this.GID=GID
        this.GIcon =GIcon
        this.GTimeStamp=GTimeStamp
        this.CreatedBy=CreatedBy
        this.GTitle=GTitle
        this.GDesc=GDesc
    }
    fun setgid(GID: String) {
        this.GID= GID
    }
    fun setTimestmp(timestamp:String){
        this.GTimeStamp =timestamp
    }
    fun setGicon(icon:String){
        this.GIcon =icon
    }
    fun setcreatedby(createdby:String){
        this.CreatedBy=createdby
    }
    fun setTitle(title:String) {
        this.GTitle= title
    }
    fun setDesc(desc:String){
        this.GDesc= desc
    }
    fun getgid():String {
        return GID
    }
    fun getTimestmp():String{
        return GTimeStamp
    }
    fun getGicon():String{
        return GIcon
    }
    fun getcreatedby():String{
        return CreatedBy
    }
    fun getTitle():String {
        return GTitle
    }
    fun getDesc():String{
        return GDesc
    }
}

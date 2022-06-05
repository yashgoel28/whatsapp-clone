package yash_g.ritik_k.YR_messenger.model

class Message {

    private var messageId: String =""
    private  var message:String = ""
    private  var senderId:String = ""
    private  var imageUrl:String =""
    private var timestamp: Long = 0
    private var feeling = -1

    constructor(){}

    constructor(message: String, senderId: String,timestamp:Long) {
        this.message = message
        this.senderId = senderId
        this.timestamp= timestamp

    }
    constructor(messageId: String,message: String, senderId: String,imageUrl: String,timestamp:Long,feeling: Int=0) {
        this.message = message
        this.messageId =messageId
        this.feeling = feeling
        this.imageUrl = imageUrl
        this.senderId = senderId
        this.timestamp= timestamp

    }
    fun setmessage(message: String) {
        this.message= message
    }
    fun setmessageId(messageId:String){
        this.messageId =messageId
    }
    fun setsenderId(senderId:String){
        this.senderId =senderId
    }
    fun settimestamp(timestamp:Long){
        this.timestamp= timestamp
    }
    fun setimageUrl(imageUrl:String) {
        this.imageUrl= imageUrl
    }
    fun setfeeling(feeling:Int){
        this.feeling= feeling
    }
    fun getmessage():String{
        return message
    }
    fun getmessageId():String{
        return messageId
    }
    fun getsenderId():String{
        return senderId
    }
    fun gettimestamp():Long{
        return timestamp
    }
    fun getimageUrl():String {
        return imageUrl
    }
    fun getfeeling():Int{
        return feeling
    }
}
package yash_g.ritik_k.YR_messenger.model

import java.sql.Timestamp

class Status {
    private var imageUrl :String?=null
    private var timestamp:Long=0

    constructor(){}
    constructor(imageUrl: String?,timestamp: Long){
        this.timestamp=timestamp
        this.imageUrl = imageUrl
    }
    fun getTimestamp():Long{
        return this.timestamp
    }
    fun setTimestamp(timestamp: Long){
        this.timestamp = timestamp
    }
    fun getImageUrl(): String? {
        return this.imageUrl
    }
    fun setImageUrl(imageUrl:String?) {
         this.imageUrl = imageUrl
    }
}
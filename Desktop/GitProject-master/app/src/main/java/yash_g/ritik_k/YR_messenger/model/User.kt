package yash_g.ritik_k.YR_messenger.model

class User {
    var uid: String = ""
    var name: String = ""
    var phoneNumber: String = ""
    var profileImage: String = ""
    var token: String = ""

    constructor() {}
    constructor(uid: String, name: String, phoneNumber: String, profileImage: String,token:String="") {
        this.uid = uid
        this.name = name
        this.phoneNumber = phoneNumber
        this.profileImage = profileImage
        this.token=token
    }
}
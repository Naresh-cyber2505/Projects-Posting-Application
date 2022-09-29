package com.example.mygramapp.model

class Notification {

    private var userid: String = ""
    private var text: String = ""
    private var postid: String = ""
    private var ispost = false

    constructor()

    constructor(userid: String, text: String, postid: String, ispost: Boolean) {
        this.userid = userid
        this.text = text
        this.postid = postid
        this.ispost = ispost
    }


    fun getUserid(): String{
        return userid
    }

    fun setUserid(userid: String){
        this.userid = userid
    }


    fun getText(): String{
        return text
    }

    fun getPostId(): String{
        return postid
    }

    fun isIsPost(): Boolean{
        return ispost
    }


    fun setText(text: String){
        this.text = text
    }
    fun setPostId(postid: String){
        this.postid = postid
    }
    fun setIsPost(ispost: Boolean){
        this.ispost = ispost
    }



}
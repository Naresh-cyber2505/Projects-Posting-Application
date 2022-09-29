package com.example.mygramapp.model

class Post {

    private var postid: String = ""
    private var postiamge: String = ""
    private var publisher: String = ""
    private var description: String = ""
    private var postedAt: Long? = 0

    constructor()

    constructor(postid: String, postiamge: String, publisher: String, description: String, postedAt: Long) {
        this.postid = postid
        this.postiamge = postiamge
        this.publisher = publisher
        this.description = description
        this.postedAt = postedAt
    }

    fun getPostid(): String{
        return postid
    }

    fun getPostimage(): String{
        return postiamge
    }


    fun getPublisher(): String{
        return publisher
    }


    fun getDescription(): String{
        return description
    }

    fun setPostid(postid: String){
        this.postid = postid
    }

    fun setPostimage(postiamge: String){
        this.postiamge = postiamge
    }

    fun setPostPublisher(publisher: String){
        this.publisher = publisher
    }

    fun setPostDescription(description: String){
        this.description = description
    }

    fun getPostedAt(): Long? {
        return postedAt
    }

    fun setPostAt(postedAt: Long){
        this.postedAt = postedAt
    }


}
package com.example.mygramapp.model

class Comments {

    private var comment: String = ""
    private var publisher: String = ""

    constructor()
    constructor(comment: String, publisher: String) {
        this.comment = comment
        this.publisher = publisher
    }

    fun getComment(): String{
        return comment
    }

    fun getPublisher(): String{
        return publisher
    }

    fun setComment(comment: String){
        this.comment = comment
    }

    fun setPublisher(publisher: String){
        this.publisher = publisher
    }

}
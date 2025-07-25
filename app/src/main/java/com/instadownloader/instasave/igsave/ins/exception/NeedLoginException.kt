package com.instadownloader.instasave.igsave.ins.exception

class NeedLoginException:Exception{

    constructor(info:String):super(info){

    }
    constructor():super(){

    }
    val log = "NeedLoginException"
}
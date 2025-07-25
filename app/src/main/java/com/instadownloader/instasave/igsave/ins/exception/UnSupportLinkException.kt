package com.instadownloader.instasave.igsave.ins.exception

class UnSupportLinkException:Exception {
    constructor(info:String):super(info){

    }
    constructor():super(){

    }
    val log = "UnSupportLinkException"
}
package com.instadownloader.instasave.igsave.ins

import java.util.regex.Pattern

//fun main(test:Array<String>){
//    println("hello world")
//
//    val url = "https://instagram.com/barackobama?igshid=YmMyMTA2M2Y="
//    val test = Test()
//    test.getHomePageNameByUrl(url)
//
//}

class Test{

    fun getHomePageNameByUrl(url:String):String{
        var match = Pattern.compile("instagram.com/(.{3,33})(\\?)").matcher(url)
        if (match.find()){
            return match.group(1)
        }
        return ""
    }
}

@file:Suppress("unused", "SameParameterValue", "MethodOverloading")

package com.instadownloader.instasave.igsave.ins


import com.google.gson.*


fun <T> Gson.safelyFromJson(data: String?, classOfT: Class<T>): T? =
    safelyMap(data) { fromJson(it, classOfT) }
fun <I, O> safelyMap(data: I?, block: (input: I) -> O): O? = try {
    data?.let { block.invoke(it) }
} catch (t: Throwable) {
    null
}

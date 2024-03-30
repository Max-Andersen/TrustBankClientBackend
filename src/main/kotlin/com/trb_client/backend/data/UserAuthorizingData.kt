package com.trb_client.backend.data

object UserAuthorizingData {
    val id = ThreadLocal<String>()
    val firebaseToken = ThreadLocal<String>()
}
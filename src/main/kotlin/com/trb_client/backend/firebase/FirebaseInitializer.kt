package com.trb_client.backend.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.stereotype.Component
import java.io.IOException
import javax.annotation.PostConstruct


@Component
class FirebaseInitializer {
    @PostConstruct
    @Throws(IOException::class)
    fun initialize() {
        val fileStream = javaClass.getClassLoader().getResourceAsStream("serviceAccountKey.json")
        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(fileStream))
            .setProjectId("trb-officer-android")
            .build()
        FirebaseApp.initializeApp(options)
    }
}
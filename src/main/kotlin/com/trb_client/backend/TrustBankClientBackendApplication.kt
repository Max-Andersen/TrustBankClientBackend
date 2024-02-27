package com.trb_client.backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TrustBankClientBackendApplication

fun main(args: Array<String>) {
//    SpringApplication.run(TrustBankClientBackendApplication::class.java, *args)
    SpringApplication.run(Service::class.java, *args)
}

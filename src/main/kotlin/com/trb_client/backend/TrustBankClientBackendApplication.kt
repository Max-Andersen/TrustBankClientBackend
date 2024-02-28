package com.trb_client.backend

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class TrustBankClientBackendApplication

fun main(args: Array<String>) {
//    SpringApplication.run(TrustBankClientBackendApplication::class.java, *args)
    SpringApplication.run(AccountOperationService::class.java, *args)
}

package com.trb_client.backend

import com.trb_client.backend.services.AccountOperationService
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
class TrustBankClientBackendApplication

fun main(args: Array<String>) {
//    SpringApplication.run(TrustBankClientBackendApplication::class.java, *args)
    runApplication<TrustBankClientBackendApplication>(*args)
}

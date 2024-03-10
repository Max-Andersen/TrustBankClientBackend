package com.trb_client.backend

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.domain.CoreRepository
import com.trb_client.backend.domain.LoanRepository
import com.trb_client.backend.domain.UserRepository
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableWebSecurity
class SecurityConfig : ApplicationContextAware {

    private lateinit var context: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    @Bean
    fun webClientBuilder(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean("coreWebClient")
    fun webCoreClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl(System.getenv("core_url")).build()
    }

    @Bean("loanWebClient")
    fun webLoanClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl(System.getenv("loan_url")).build()
    }

    @Bean("usersWebClient")
    fun webUsersClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl(System.getenv("users_url")).build()
    }

    @Bean
    fun coreRequest(): CoreRepository {
        val webClient = context.getBean("coreWebClient", WebClient::class.java)
        return CoreRepository(webClient)
    }

    @Bean
    fun userRequest(): UserRepository {
        val webClient = context.getBean("usersWebClient", WebClient::class.java)
        return UserRepository(webClient)
    }

    @Bean
    fun loanRequest(): LoanRepository {
        val webClient = context.getBean("loanWebClient", WebClient::class.java)
        return LoanRepository(webClient)
    }

}


class AccountOperationsServer(private val port: Int) {
    private val accountOperationService: AccountOperationService
    private val userOperationService: UserOperationService
    private val loanOperationService: LoanOperationService

    init {
        val context: ApplicationContext = AnnotationConfigApplicationContext(SecurityConfig::class.java)
        val coreRepository = context.getBean(CoreRepository::class.java)
        val userRepository = context.getBean(UserRepository::class.java)
        val loanRepository = context.getBean(LoanRepository::class.java)
        accountOperationService =
            AccountOperationService(coreRepository, userRepository)
        userOperationService = UserOperationService(userRepository)
        loanOperationService = LoanOperationService(loanRepository)

        println(System.getenv("core_url"))
        println(System.getenv("loan_url"))
        println(System.getenv("users_url"))
    }


    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(ServerInterceptors.intercept(accountOperationService, HeaderServerInterceptor()))
        .addService(ServerInterceptors.intercept(userOperationService, HeaderServerInterceptor()))
        .addService(ServerInterceptors.intercept(loanOperationService, HeaderServerInterceptor()))
        .build()

    fun start() {
        server.start()
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@AccountOperationsServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

fun main() {
    val port = 50051
    val server = AccountOperationsServer(port)
    server.start()
    server.blockUntilShutdown()
}
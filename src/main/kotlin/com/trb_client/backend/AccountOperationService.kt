package com.trb_client.backend

import com.google.protobuf.Timestamp
import com.trustbank.client_mobile.proto.*
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status.*
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager


@GrpcService
class AccountOperationService(
    val authenticationManager: AuthenticationManager,
    val coreRequest: CoreRequest
) : AccountOperationsServiceGrpc.AccountOperationsServiceImplBase() {


    override fun login(request: LoginRequest, responseObserver: StreamObserver<Client>) {
        val clientId = coreRequest.credentials.getOrDefault(request, null)
        println(clientId)
        clientId?.let {
            responseObserver.onNext(coreRequest.users.find { it.id == clientId })
            responseObserver.onCompleted()
        } ?: responseObserver.onError(UNAUTHENTICATED.asRuntimeException())
    }

    override fun getAccounts(request: GetAccountsRequest, responseObserver: StreamObserver<Account>) {
        val accounts = coreRequest.accounts.filter { it.owner.id == request.userId }

        if (accounts.isEmpty()) {
            responseObserver.onError(NOT_FOUND.withDescription("Аккаунты не найдены").asRuntimeException())
        } else {
            accounts.forEach {
                responseObserver.onNext(it)
            }
            responseObserver.onCompleted()
        }
    }

    override fun openNewAccount(request: OpenAccountRequest, responseObserver: StreamObserver<OperationResponse>) {
        val client = coreRequest.users.find { it.id == request.userId }
        client?.let {
            val account = Account.newBuilder()
                .setId((coreRequest.accounts.size + 1).toString())
                .setOwner(client)
                .setBalance(0)
                .setCreationDate(Timestamp.newBuilder().setSeconds(1709124966))
                .build()
            coreRequest.accounts.add(account)
            println(coreRequest.accounts.size)
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } ?: responseObserver.onError(NOT_FOUND.withDescription("Пользователь не найден").asRuntimeException())
    }

    override fun closeAccount(request: CloseAccountRequest, responseObserver: StreamObserver<OperationResponse>) {
        val account = coreRequest.accounts.find { it.id == request.accountId }
        account?.let {
            coreRequest.accounts.remove(account)
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } ?: responseObserver.onError(NOT_FOUND.withDescription("Аккаунт не найден").asRuntimeException())
    }


    override fun depositMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {
        val account = coreRequest.accounts.find { it.id == request.accountId }
        account?.let {
            val newBalance = it.balance + request.amount
            coreRequest.accounts.remove(account)
            coreRequest.accounts.add(account.toBuilder().setBalance(newBalance).build())
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } ?: responseObserver.onError(NOT_FOUND.withDescription("Аккаунт не найден").asRuntimeException())
    }

    override fun withdrawMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {
        val account = coreRequest.accounts.find { it.id == request.accountId }
        account?.let {
            val newBalance = it.balance - request.amount
            if (newBalance>=0) {
                coreRequest.accounts.remove(account)
                coreRequest.accounts.add(account.toBuilder().setBalance(newBalance).build())
                responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
                responseObserver.onCompleted()
            } else {
                responseObserver.onError(CANCELLED.withDescription("Недостаточно средств").asRuntimeException())
            }
        } ?: responseObserver.onError(NOT_FOUND.withDescription("Аккаунт не найден").asRuntimeException())
    }

    override fun getHistoryOfAccount(
        request: GetHistoryOfAccountRequest,
        responseObserver: StreamObserver<Transaction>
    ) {
        val account = coreRequest.accounts.find { it.id == request.accountId }
        account?.let {
            val transactions = mutableListOf(
                Transaction.newBuilder()
                    .setAmount(100)
                    .setDate(Timestamp.newBuilder().setSeconds(1709124966))
                    .build()
            )
            transactions.forEach {
                responseObserver.onNext(it)
            }
            responseObserver.onCompleted()
        } ?: responseObserver.onError(NOT_FOUND.withDescription("Аккаунт не найден").asRuntimeException())
    }

    override fun helloWorld(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
//        responseObserver.onNext(HelloResponse.newBuilder().setMessage("Hello ${request.name}").build())
//        responseObserver.onCompleted()
        val authenticationRequest =
            UsernamePasswordAuthenticationToken.unauthenticated(
                "user", "password"
            )
        val authenticationResponse =
            authenticationManager.authenticate(authenticationRequest)


        responseObserver.onNext(
            HelloResponse.newBuilder().setMessage("${authenticationResponse.isAuthenticated}").build()
        )
        responseObserver.onCompleted()


//        responseObserver.onError(
//            NOT_FOUND.withDescription("This pet with id = " + request.name + " is not found")
//                .asRuntimeException()
//        )

//        responseObserver.onError(Throwable("Error"))

//        val errorResponseKey: Metadata.Key<PetOuterClass.ErrorResponse> =
//            ProtoUtils.keyForProto(PetOuterClass.ErrorResponse.getDefaultInstance())
//        val errorResponse: PetOuterClass.ErrorResponse = PetOuterClass.ErrorResponse.newBuilder()
//            .setErrorName(("This pet with id = " + request.getPetId()).toString() + " is not in the database")
//            .build()
//        val metadata = Metadata()
//        metadata.put(errorResponseKey, errorResponse)


    }


}


class CoreRequest {
    val users = mutableListOf(
        run {
            val builder = Client.newBuilder()
            builder
                .setId("999")
                .setFirstName("user")
                .setLastName("userov")
                .setPatronymic("userovich")
                .setPhoneNumber("1234567890")
                .setAddress("ylitsa pushkina dom kolotushkina")
                .setPassportNumber("69139999")
                .setPassportSeries("999999")
                .setIsBlocked(false)
            builder.build()
        }
    )

    val credentials = mutableMapOf(
        LoginRequest.newBuilder().setLogin("user").setPassword("password").build() to "999"
    )

    val accounts = mutableListOf(
        run {
            Timestamp.newBuilder().setSeconds(1).build()

            val builder = Account.newBuilder()
            builder.id = 1.toString()
            builder.owner = users.find { it.id == "999" }
            builder.balance = 1000L
            builder.creationDateBuilder.setSeconds(1709124966)
            builder.build()
        }
    )
}


@Configuration
@EnableWebSecurity
class SecurityConfig {

    //    @Bean
//    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
//        http {
//            authorizeHttpRequests {
//                authorize("/login", permitAll)
//                authorize(anyRequest, authenticated)
//            }
//        }
//
//        return http.build()
//    }


    @Bean
    fun authenticationManager(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder
    ): AuthenticationManager {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService)
        authenticationProvider.setPasswordEncoder(passwordEncoder)

        return ProviderManager(authenticationProvider)
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        val user = User.withDefaultPasswordEncoder()
            .username("user")
            .password("password")
            .roles("USER")
            .build()

        return InMemoryUserDetailsManager(user)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

}

class AccountOperationsServer(private val port: Int) {
    private val accountOperationService: AccountOperationService

    init {
        val context: ApplicationContext = AnnotationConfigApplicationContext(SecurityConfig::class.java)
        val authenticationManager = context.getBean(AuthenticationManager::class.java)
        accountOperationService = AccountOperationService(authenticationManager, CoreRequest())
        // Используйте accountOperationService
    }


    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(accountOperationService)
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
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
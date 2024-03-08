package com.trb_client.backend

import com.google.protobuf.Timestamp
import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.CoreRequestRepository
import com.trb_client.backend.domain.UserRequestRepository
import com.trb_client.backend.mapper.toAccountGrpc
import com.trb_client.backend.mapper.toClient
import com.trb_client.backend.models.AccountType
import com.trustbank.client_mobile.proto.*
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors
import io.grpc.Status.*
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
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
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID


@GrpcService
class AccountOperationService(
    val authenticationManager: AuthenticationManager,
    val coreRequestRepository: CoreRequestRepository,
    val userRequestRepository: UserRequestRepository
) : AccountOperationsServiceGrpc.AccountOperationsServiceImplBase() {

    /**
     *
     * user
     * 173ea10f-0915-4c47-a8a3-d293f0aa24bc
     *
     *
     * accounts
     * 2a149215-7f21-4bf3-966f-6d56cb3c00f6
     *
     */

    override fun login(request: LoginRequest, responseObserver: StreamObserver<Client>) {
        val clientInfo = userRequestRepository.login(request.login, request.password)
        clientInfo?.let {
            val client = it.toClient()
            responseObserver.onNext(client)
            responseObserver.onCompleted()
        } ?: responseObserver.onError(UNAUTHENTICATED.asRuntimeException())
    }

    override fun getAccounts(request: GetAccountsRequest, responseObserver: StreamObserver<Account>) {
        val userId = UserAuthorizingData.id.get()
        val accounts = coreRequestRepository.getClientAccounts(UUID.fromString(userId))

        val owner: Client? =
            if (accounts.isNotEmpty())
                (userRequestRepository.getClientById(accounts.first().externalClientId.toString())
                    ?: throw Exception("user not found")).toClient()
            else null

        accounts.forEach { account ->
            responseObserver.onNext(account.toAccountGrpc(owner!!))
        }
        responseObserver.onCompleted()
    }

    override fun getAccountInfo(request: GetAccountInfoRequest, responseObserver: StreamObserver<Account>) {
        val account = coreRequestRepository.getAccountInfo(UUID.fromString(request.accountId))

        val accountResponse = Account.newBuilder()
            .setId(account.id.toString())
            .setOwner(
                Client.newBuilder()
                    .setId(account.externalClientId.toString())
                    .setFirstName(account.clientFullName)
                    .build()
            )
            .setBalance(account.balance)
            .setCreationDate(Timestamp.newBuilder().setSeconds(account.creationDate?.toInstant()?.epochSecond ?: 0))
            .setClosingDate(Timestamp.newBuilder().setSeconds(account.closingDate?.toInstant()?.epochSecond ?: 0))
            .setOwnerFullName(account.clientFullName)
            .setType(account.type?.let { com.trustbank.client_mobile.proto.AccountType.valueOf(it.name) })
            .build()
        responseObserver.onNext(accountResponse)
        responseObserver.onCompleted()
    }


    override fun openNewAccount(request: OpenAccountRequest, responseObserver: StreamObserver<OperationResponse>) {
        try {
            coreRequestRepository.createAccount(
                UUID.fromString(request.userId),
                "Иванов Иван Иванович", // TODO в сервис пользователей
                AccountType.DEPOSIT
            )

            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка создания аккаунта").asRuntimeException())
        }
    }

    override fun closeAccount(request: CloseAccountRequest, responseObserver: StreamObserver<OperationResponse>) {
        try {
            coreRequestRepository.closeAccount(UUID.fromString(request.accountId))
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка закрытия аккаунта").asRuntimeException())
        }
    }


    override fun depositMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {
        try {
            coreRequestRepository.depositMoney(UUID.fromString(request.accountId), request.amount)
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка пополнения счета").asRuntimeException())
        }
    }

    override fun withdrawMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {

        try {
            coreRequestRepository.withdrawMoney(UUID.fromString(request.accountId), request.amount)
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка снятия средств").asRuntimeException())
        }

//        val account = coreRequestRepository.accounts.find { it.id == request.accountId }
//        account?.let {
//            val newBalance = it.balance - request.amount
//            if (newBalance >= 0) {
//                coreRequestRepository.accounts.add(
//                    coreRequestRepository.accounts.indexOf(account),
//                    account.toBuilder().setBalance(newBalance).build()
//                )
//                responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
//                responseObserver.onCompleted()
//            } else {
//                responseObserver.onError(CANCELLED.withDescription("Недостаточно средств").asRuntimeException())
//            }
//        } ?: responseObserver.onError(NOT_FOUND.withDescription("Аккаунт не найден").asRuntimeException())
    }

    override fun getHistoryOfAccount(
        request: GetHistoryOfAccountRequest,
        responseObserver: StreamObserver<com.trustbank.client_mobile.proto.TransactionHistoryPage>
    ) {
        try {
            val page = coreRequestRepository.getAccountHistory(
                UUID.fromString(request.accountId),
                request.pageNumber,
                request.pageSize
            )
            responseObserver.onNext(
                com.trustbank.client_mobile.proto.TransactionHistoryPage.newBuilder().setPageNumber(page.pageNumber)
                    .setPageSize(page.pageSize).setPageNumber(page.pageNumber)
                    .addAllElements(page.elements.map {
                        Transaction.newBuilder()
                            .setAmount(it.amount)
                            .setDate(Timestamp.newBuilder().setSeconds(it.date?.toInstant()?.epochSecond ?: 0L))
                            .build()
                    }).build()
            )
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка получения истории операций").asRuntimeException())
        }


//        val account = coreRequestRepository.accounts.find { it.id == request.accountId }
//        account?.let {
//            val transactions = mutableListOf(
//                Transaction.newBuilder()
//                    .setAmount(100)
//                    .setDate(Timestamp.newBuilder().setSeconds(1709124966))
//                    .build()
//            )
//            transactions.forEach {
//                responseObserver.onNext(it)
//            }
//            responseObserver.onCompleted()
//        } ?: responseObserver.onError(NOT_FOUND.withDescription("Аккаунт не найден").asRuntimeException())
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


//        responseObserver.onNext(
//            HelloResponse.newBuilder().setMessage("${authenticationResponse.isAuthenticated}").build()
//        )
//        responseObserver.onCompleted()


        responseObserver.onError(
            NOT_FOUND.withDescription("This pet with id = " + request.name + " is not found")
                .asRuntimeException()
        )

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


/**
 *         val accountId = "a4a3674b-4332-4264-81c6-fd812b353639"
 *         val url = "/api/v1/accounts/$accountId"
 *         val response = webClient.get()
 *             .uri(url)
 *             .retrieve()
 *             .bodyToMono(String::class.java)
 *             .block()
 *
 *         println(response)
 */


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

    @Bean
    fun webClient(builder: WebClient.Builder): WebClient {
        return builder.baseUrl("http://localhost:8080").build()
    }

    @Bean
    fun coreRequest(): CoreRequestRepository {
        val webClient = context.getBean(WebClient::class.java)
        return CoreRequestRepository(webClient)
    }

    @Bean
    fun userRequest(): UserRequestRepository {
        val webClient = context.getBean(WebClient::class.java)
        return UserRequestRepository(webClient)
    }


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
        val coreRequestRepository = context.getBean(CoreRequestRepository::class.java)
        val userRequestRepository = context.getBean(UserRequestRepository::class.java)
        accountOperationService =
            AccountOperationService(authenticationManager, coreRequestRepository, userRequestRepository)
    }


    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(ServerInterceptors.intercept(accountOperationService, HeaderServerInterceptor()))
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
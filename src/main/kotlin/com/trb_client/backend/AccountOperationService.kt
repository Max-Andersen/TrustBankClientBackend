package com.trb_client.backend

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.CoreRepository
import com.trb_client.backend.domain.LoanRepository
import com.trb_client.backend.domain.UserRepository
import com.trb_client.backend.mapper.toGrpc
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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.web.reactive.function.client.WebClient
import java.util.UUID


@GrpcService
class AccountOperationService(
    val coreRepository: CoreRepository,
    val userRepository: UserRepository
) : AccountOperationServiceGrpc.AccountOperationServiceImplBase() {

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

//    override fun login(request: LoginRequest, responseObserver: StreamObserver<Client>) {
//        val clientInfo = userRequestRepository.login(request.login, request.password)
//        clientInfo?.let {
//            val client = it.toGrpc()
//            responseObserver.onNext(client)
//            responseObserver.onCompleted()
//        } ?: responseObserver.onError(UNAUTHENTICATED.asRuntimeException())
//    }

    override fun getAccounts(request: GetAccountsRequest, responseObserver: StreamObserver<Account>) {
        val userId = UserAuthorizingData.id.get()
        val accounts = coreRepository.getClientAccounts(UUID.fromString(userId))

        val owner: Client? =
            if (accounts.isNotEmpty())
                (userRepository.getClientById(accounts.first().externalClientId.toString())
                    ?: throw Exception("user not found")).toGrpc()
            else null

        accounts.forEach { account ->
            responseObserver.onNext(account.toGrpc(owner))
        }
        responseObserver.onCompleted()
    }

    override fun getAccountInfo(request: GetAccountInfoRequest, responseObserver: StreamObserver<Account>) {
        val userId = UserAuthorizingData.id.get()
        try {
            val accountInfo = coreRepository.getAccountInfo(UUID.fromString(request.accountId))
            val owner = userRepository.getClientById(accountInfo.externalClientId.toString())
                ?: throw Exception("user not found")

            val accountResponse = accountInfo.toGrpc(owner.toGrpc())
            responseObserver.onNext(accountResponse)
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                INTERNAL.withDescription("Ошибка получения информации об аккаунте").asRuntimeException()
            )
        }
    }


    override fun openNewAccount(request: OpenAccountRequest, responseObserver: StreamObserver<OperationResponse>) {
        try {
            val userId = UserAuthorizingData.id.get()
            val user = userRepository.getClientById(userId)
                ?: throw Exception("user not found")

            coreRepository.createAccount(
                clientId = UUID.fromString(userId),
                clientFullName = user.firstName + user.lastName + user.patronymic,
                accountType = AccountType.DEPOSIT
            )

            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка создания аккаунта").asRuntimeException())
        }
    }

    override fun closeAccount(request: CloseAccountRequest, responseObserver: StreamObserver<OperationResponse>) {
        try {
            coreRepository.closeAccount(UUID.fromString(request.accountId))
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка закрытия аккаунта").asRuntimeException())
        }
    }

    override fun transferMoney(request: TransferMoneyRequest, responseObserver: StreamObserver<Transaction>) {
        try {
            val transaction = coreRepository.transferMoney(
                UUID.fromString(request.fromAccountId),
                UUID.fromString(request.toAccountId),
                request.amount
            )
            val payerAccount = transaction.payerAccountId?.let {
                coreRepository.getAccountInfo(it)
            }
            val payer = payerAccount?.let {
                userRepository.getClientById(it.externalClientId.toString())
            }?.toGrpc()
            val payeeAccount = transaction.payeeAccountId?.let {
                coreRepository.getAccountInfo(it)
            }
            val payee = payeeAccount?.let {
                userRepository.getClientById(it.externalClientId.toString())
            }?.toGrpc()

            responseObserver.onNext(transaction.toGrpc(payeeAccount?.toGrpc(payee), payerAccount?.toGrpc(payer)))
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка перевода средств").asRuntimeException())
        }
    }


    override fun depositMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {
        try {
            coreRepository.depositMoney(UUID.fromString(request.accountId), request.amount)
            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка пополнения счета").asRuntimeException())
        }
    }

    override fun withdrawMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {
        try {
            coreRepository.withdrawMoney(UUID.fromString(request.accountId), request.amount)
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
        responseObserver: StreamObserver<TransactionHistoryPage>
    ) {
        try {
            val page = coreRepository.getAccountHistory(
                UUID.fromString(request.accountId),
                request.pageNumber,
                request.pageSize
            )
            responseObserver.onNext(
                TransactionHistoryPage.newBuilder().setPageNumber(page.pageNumber)
                    .setPageSize(page.pageSize).setPageNumber(page.pageNumber)
                    .addAllElements(page.elements.map { transactionItem ->
                        val payerAccount = transactionItem.payerAccountId?.let {
                            coreRepository.getAccountInfo(it)
                        }
                        val payer = payerAccount?.let {
                            it.externalClientId?.let { clientId -> userRepository.getClientById(clientId) }
                        }?.toGrpc()
                        val payeeAccount = transactionItem.payeeAccountId?.let {
                            coreRepository.getAccountInfo(it)
                        }
                        val payee = payeeAccount?.let {
                            it.externalClientId?.let { clientId -> userRepository.getClientById(clientId) }
                        }?.toGrpc()

                        transactionItem.toGrpc(
                            payee = payeeAccount?.toGrpc(payee),
                            payer = payerAccount?.toGrpc(payer)
                        )
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

//    override fun helloWorld(request: HelloRequest, responseObserver: StreamObserver<HelloResponse>) {
////        responseObserver.onNext(HelloResponse.newBuilder().setMessage("Hello ${request.name}").build())
////        responseObserver.onCompleted()
//        val authenticationRequest =
//            UsernamePasswordAuthenticationToken.unauthenticated(
//                "user", "password"
//            )
//        val authenticationResponse =
//            authenticationManager.authenticate(authenticationRequest)
//
//
////        responseObserver.onNext(
////            HelloResponse.newBuilder().setMessage("${authenticationResponse.isAuthenticated}").build()
////        )
////        responseObserver.onCompleted()
//
//
//        responseObserver.onError(
//            NOT_FOUND.withDescription("This pet with id = " + request.name + " is not found")
//                .asRuntimeException()
//        )
//
////        responseObserver.onError(Throwable("Error"))
//
////        val errorResponseKey: Metadata.Key<PetOuterClass.ErrorResponse> =
////            ProtoUtils.keyForProto(PetOuterClass.ErrorResponse.getDefaultInstance())
////        val errorResponse: PetOuterClass.ErrorResponse = PetOuterClass.ErrorResponse.newBuilder()
////            .setErrorName(("This pet with id = " + request.getPetId()).toString() + " is not in the database")
////            .build()
////        val metadata = Metadata()
////        metadata.put(errorResponseKey, errorResponse)
//
//
//    }


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

    @Bean("coreWebClient")
    fun webCoreClient(builder: WebClient.Builder): WebClient {
        println(System.getenv("core_url"))
        println(System.getenv("core_url"))
        println(System.getenv("core_url"))
        println(System.getenv("core_url"))
        println(System.getenv("core_url"))
        println(System.getenv("core_url"))
        println(System.getenv("core_url"))
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
    }


    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(ServerInterceptors.intercept(accountOperationService, HeaderServerInterceptor()))
        .addService(ServerInterceptors.intercept(userOperationService, HeaderServerInterceptor()))
        .addService(ServerInterceptors.intercept(loanOperationService, HeaderServerInterceptor()))
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
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
    }
}

package com.trb_client.backend.services

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.CoreRepository
import com.trb_client.backend.domain.HiddenAccountRepository
import com.trb_client.backend.domain.UserRepository
import com.trb_client.backend.kafka.consume.TransactionCallbackConsumer
import com.trb_client.backend.mapper.toGrpc
import com.trb_client.backend.models.AccountType
import com.trustbank.client_mobile.proto.*
import io.grpc.Status.INTERNAL
import io.grpc.Status.UNAUTHENTICATED
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.runBlocking
import net.devh.boot.grpc.server.service.GrpcService
import org.apache.catalina.User
import java.util.*


@GrpcService(interceptors = [HeaderServerInterceptor::class])
class AccountOperationService(
    val coreRepository: CoreRepository,
    val userRepository: UserRepository,
    val hidedAccountRepository: HiddenAccountRepository,
    val transactionCallbackConsumer: TransactionCallbackConsumer
) : AccountOperationServiceGrpc.AccountOperationServiceImplBase() {

    override fun getAccounts(request: GetAccountsRequest, responseObserver: StreamObserver<Account>) {
        val userId = UserAuthorizingData.id.get()
        val accounts = coreRepository.getClientAccounts(UUID.fromString(userId))

        val hidedAccounts =
            listOf<String>() //hidedAccountRepository.getHiddenAccounts(UserAuthorizingData.firebaseToken.get())

        val owner: Client? =
            if (accounts.isNotEmpty())
                (userRepository.getClientById(accounts.first().externalClientId.toString())
                    ?: throw Exception("user not found")).toGrpc()
            else null

        accounts.forEach { account ->
            responseObserver.onNext(account.toGrpc(owner, account.id.toString() in hidedAccounts))
        }
        responseObserver.onCompleted()
    }

    override fun getAccountInfo(request: GetAccountInfoRequest, responseObserver: StreamObserver<Account>) {
        val userId = UserAuthorizingData.id.get()
        try {
            val accountInfo = coreRepository.getAccountInfo(UUID.fromString(request.accountId))

            if (userId == accountInfo.externalClientId) {
                val owner = userRepository.getClientById(accountInfo.externalClientId.toString())
                    ?: throw Exception("user not found")
                val hidedAccounts =
                    listOf<String>() //hidedAccountRepository.getHiddenAccounts(UserAuthorizingData.firebaseToken.get())

                val accountResponse = accountInfo.toGrpc(owner.toGrpc(), accountInfo.id.toString() in hidedAccounts)
                responseObserver.onNext(accountResponse)
            } else {
                responseObserver.onError(
                    UNAUTHENTICATED.withDescription("Счет принадлежит другому человеку").asRuntimeException()
                )
            }
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
                accountType = AccountType.DEPOSIT,
                currency = request.currency
            )

            responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка создания аккаунта").asRuntimeException())
        }
    }

    override fun closeAccount(request: CloseAccountRequest, responseObserver: StreamObserver<OperationResponse>) {

        val account = coreRepository.getAccountInfo(UUID.fromString(request.accountId))

        if (account.externalClientId == UserAuthorizingData.id.get()) {
            try {
                coreRepository.closeAccount(UUID.fromString(request.accountId))
                responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(INTERNAL.withDescription("Ошибка закрытия аккаунта").asRuntimeException())
            }
        } else {
            responseObserver.onError(
                UNAUTHENTICATED.withDescription("Счет принадлежит другому человеку").asRuntimeException()
            )
        }
        responseObserver.onCompleted()

    }

    override fun transferMoney(request: TransferMoneyRequest, responseObserver: StreamObserver<Transaction>) {
        try {
            val payerAccount = request.fromAccountId?.let {
                coreRepository.getAccountInfo(UUID.fromString(request.fromAccountId))
            }

            if (payerAccount?.externalClientId == UserAuthorizingData.id.get()) {
                val transaction = coreRepository.transferMoney(
                    UUID.fromString(request.fromAccountId),
                    UUID.fromString(request.toAccountId),
                    request.amount
                )
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
            } else {
                responseObserver.onError(
                    UNAUTHENTICATED.withDescription("Счет принадлежит другому человеку").asRuntimeException()
                )
            }

            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(INTERNAL.withDescription("Ошибка перевода средств").asRuntimeException())
        }
    }


    override fun depositMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {
        val account = coreRepository.getAccountInfo(UUID.fromString(request.accountId))

        if (account.externalClientId == UserAuthorizingData.id.get()) {
            try {
                coreRepository.depositMoney(UUID.fromString(request.accountId), request.amount, request.currency)
                responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(INTERNAL.withDescription("Ошибка пополнения счета").asRuntimeException())
            }
        } else {
            responseObserver.onError(
                UNAUTHENTICATED.withDescription("Счет принадлежит другому человеку").asRuntimeException()
            )
        }
        responseObserver.onCompleted()

    }

    override fun withdrawMoney(request: MoneyOperation, responseObserver: StreamObserver<OperationResponse>) {
        val account = coreRepository.getAccountInfo(UUID.fromString(request.accountId))

        if (account.externalClientId == UserAuthorizingData.id.get()) {
            try {
                coreRepository.withdrawMoney(UUID.fromString(request.accountId), request.amount, request.currency)
                responseObserver.onNext(OperationResponse.newBuilder().setSuccess(true).build())
                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(INTERNAL.withDescription("Ошибка снятия средств").asRuntimeException())
            }
        } else {
            responseObserver.onError(
                UNAUTHENTICATED.withDescription("Счет принадлежит другому человеку").asRuntimeException()
            )
        }

    }

    override fun getHistoryOfAccount(
        request: GetHistoryOfAccountRequest,
        responseObserver: StreamObserver<Transaction>
    ) {

        val account = coreRepository.getAccountInfo(UUID.fromString(request.accountId))


        if (account.externalClientId == UserAuthorizingData.id.get()){
            try {
                val page = coreRepository.getAccountHistory(
                    UUID.fromString(request.accountId),
                    request.pageNumber,
                    request.pageSize
                )

                page.elements.map { transactionItem ->
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
                }.forEach {
                    responseObserver.onNext(it)
                }

                runBlocking {
                    transactionCallbackConsumer.flow.collect {
                        println("transaction    " + it)
                    }
                }

                responseObserver.onCompleted()
            } catch (e: Exception) {
                responseObserver.onError(INTERNAL.withDescription("Ошибка получения истории операций").asRuntimeException())
            }
        } else{
            responseObserver.onError(
                UNAUTHENTICATED.withDescription("Счет принадлежит другому человеку").asRuntimeException()
            )
        }
    }
}

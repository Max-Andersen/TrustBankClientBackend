package com.trb_client.backend.domain

import com.trb_client.backend.kafka.produce.TransactionInit
import com.trb_client.backend.kafka.produce.TransactionInitProducer
import com.trb_client.backend.models.AccountType
import com.trb_client.backend.models.request.Currency
import com.trb_client.backend.models.request.NewAccountRequest
import com.trb_client.backend.models.request.TransferMoneyRequest
import com.trb_client.backend.models.request.UnidirectionalTransactionRequest
import com.trb_client.backend.models.response.AccountResponse
import com.trb_client.backend.models.response.Transaction
import com.trb_client.backend.models.response.TransactionHistoryPage
import com.trb_client.backend.models.response.TransactionType
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

class CoreRepository(
    private val webClient: WebClient,
    private val transactionInitProducer: TransactionInitProducer
) {
    private val baseSubCoreUrl = "/api/v1/"

    fun transferMoney(fromAccountId: UUID, toAccountId: UUID, amount: Double, currency: Currency) {
//        val url = "${baseSubCoreUrl}transactions/account-to-account"
//        val requestModel = TransferMoneyRequest(
//            fromAccountId.toString(),
//            toAccountId.toString(),
//            amount,
//        )
//        val response =
//            webClient.post().uri(url).bodyValue(requestModel).exchangeToMono { it.toEntity(Transaction::class.java) }
//                .block()

        val uuid = UUID.randomUUID()
        val transactionInit =
            TransactionInit(fromAccountId, toAccountId, amount, currency , TransactionType.TRANSFER)
        transactionInitProducer.sendMessage(uuid, transactionInit)

//        response?.let {
//            if (it.statusCode.is2xxSuccessful) {
//                 TODO проверить всё
//                return it.body ?: throw Exception("Transfer not created")
//            }
//        }
//
//        throw Exception("Transfer response is null or not successful")
    }

    fun withdrawMoney(accountId: UUID, amount: Double, currency: String): Boolean {
//        val url = "${baseSubCoreUrl}transactions/withdrawal"
//        val requestModel = UnidirectionalTransactionRequest(accountId, amount)
//        val response =
//            webClient.post().uri(url).bodyValue(requestModel).exchangeToMono { it.toEntity(String::class.java) }.block()
//
//        return response?.statusCode?.is2xxSuccessful ?: throw Exception("Response is null or not successful")
        val uuid = UUID.randomUUID()
        val transactionInit =
            TransactionInit(accountId, null, amount, Currency.valueOf(currency) , TransactionType.WITHDRAWAL)
        transactionInitProducer.sendMessage(uuid, transactionInit)

        return true
    }

    fun depositMoney(accountId: UUID, amount: Double, currency: String): Boolean {
//        val url = "${baseSubCoreUrl}transactions/replenishment"
//        val requestModel = UnidirectionalTransactionRequest(accountId, amount)
//        val response =
//            webClient.post().uri(url).bodyValue(requestModel).exchangeToMono { it.toEntity(String::class.java) }.block()

        val uuid = UUID.randomUUID()
        val transactionInit =
            TransactionInit(null, accountId, amount, Currency.valueOf(currency) , TransactionType.REPLENISHMENT)
        transactionInitProducer.sendMessage(uuid, transactionInit)

        return true
//        return response?.statusCode?.is2xxSuccessful ?: throw Exception("Response is null or not successful")
    }

    fun createAccount(clientId: UUID, clientFullName: String, currency: String, accountType: AccountType): AccountResponse {
        val url = "${baseSubCoreUrl}accounts"
        val requestModel =
            NewAccountRequest(type = accountType, currency = Currency.valueOf(currency), clientFullName = clientFullName, externalClientId = clientId)
        val response =
            webClient.post().uri(url).bodyValue(requestModel)
                .exchangeToMono { it.toEntity(AccountResponse::class.java) }.block()

        response?.let {
            if (it.statusCode.is2xxSuccessful) {
                // TODO проверить всё
                return it.body ?: throw Exception("Account not created")
            }
        }

        throw Exception("Response is null or not successful")
    }

    fun getClientAccounts(clientId: UUID): List<AccountResponse> {
        val url = "${baseSubCoreUrl}users/$clientId/accounts"
        val response =
            webClient.get().uri(url).exchangeToMono { it.toEntityList(AccountResponse::class.java) }.block()

        response?.let {
            if (it.statusCode.is2xxSuccessful) {
                // TODO проверить всё
                return it.body ?: throw Exception("Account not created")
            }
        }

        throw Exception("Response is null or not successful")
    }

    fun getAccountInfo(accountId: UUID): AccountResponse {
        val url = "${baseSubCoreUrl}accounts/$accountId"
        val response =
            webClient.get().uri(url).exchangeToMono { it.toEntity(AccountResponse::class.java) }.block()

        response?.let {
            if (it.statusCode.is2xxSuccessful) {
                // TODO проверить всё
                return it.body ?: throw Exception("Account not found")
            }
        }

        throw Exception("Response is null or not successful")
    }

    fun closeAccount(accountId: UUID): Boolean {
        val url = "${baseSubCoreUrl}accounts/$accountId"
        val response =
            webClient.delete().uri(url).exchangeToMono { it.toEntity(String::class.java) }.block()

        response?.let {
            return it.statusCode.is2xxSuccessful
        }

        throw Exception("Response is null or not successful")
    }

    fun getAccountHistory(accountId: UUID, page: Int, pageSize: Int): TransactionHistoryPage {
        val url = "${baseSubCoreUrl}accounts/$accountId/history"
        val response =
            webClient.get().uri { uriBuilder ->
                uriBuilder
                    .path(url)
                    .queryParam("page", page)
                    .queryParam("size", pageSize)
                    .build()
            }.exchangeToMono { it.toEntity(TransactionHistoryPage::class.java) }.block()

        response?.let {
            if (it.statusCode.is2xxSuccessful) {
                // TODO проверить всё
                return it.body ?: throw Exception("Account not found")
            }
        }

        throw Exception("Response is null or not successful")
    }

}
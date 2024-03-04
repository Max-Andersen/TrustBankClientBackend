package com.trb_client.backend.domain

import com.google.protobuf.Timestamp
import com.trb_client.backend.models.AccountType
import com.trb_client.backend.models.request.NewAccountRequest
import com.trb_client.backend.models.request.UnidirectionalTransactionRequest
import com.trb_client.backend.models.response.AccountResponse
import com.trb_client.backend.models.response.TransactionHistoryPage
import com.trustbank.client_mobile.proto.Account
import com.trustbank.client_mobile.proto.Client
import com.trustbank.client_mobile.proto.LoginRequest
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import java.awt.PageAttributes
import java.util.*

class CoreRequestRepository(
    private val webClient: WebClient
) {
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

    fun withdrawMoney(accountId: UUID, amount: Long): Boolean {
        val url = "/api/v1/transactions/withdraw"
        val requestModel = UnidirectionalTransactionRequest(accountId, amount)
        val response =
            webClient.post().uri(url).bodyValue(requestModel).exchangeToMono { it.toEntity(String::class.java) }.block()

        return response?.statusCode?.is2xxSuccessful ?: false
    }

    fun depositMoney(accountId: UUID, amount: Long): Boolean {
        val url = "/api/v1/transactions/replenishment"
        val requestModel = UnidirectionalTransactionRequest(accountId, amount)
        val response =
            webClient.post().uri(url).bodyValue(requestModel).exchangeToMono { it.toEntity(String::class.java) }.block()

        return response?.statusCode?.is2xxSuccessful ?: false
    }

    fun createAccount(clientId: UUID, clientFullName: String, accountType: AccountType): AccountResponse {
        val url = "/api/v1/accounts"
        val requestModel = NewAccountRequest(accountType, clientFullName, clientId)
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
        val url = "/api/v1/users/$clientId/accounts"
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
        val url = "/api/v1/accounts/$accountId"
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
        val url = "/api/v1/accounts/$accountId"
        val response =
            webClient.delete().uri(url).exchangeToMono { it.toEntity(String::class.java) }.block()

        response?.let {
            return it.statusCode.is2xxSuccessful
        }

        throw Exception("Response is null or not successful")
    }

    fun getAccountHistory(accountId: UUID, page: Int, pageSize: Int): TransactionHistoryPage {
        val url = "/api/v1/accounts/$accountId/history"
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
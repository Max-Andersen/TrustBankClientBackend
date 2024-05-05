package com.trb_client.backend.domain

import com.trb_client.backend.models.request.Currency
import com.trb_client.backend.models.request.LoanRequest
import com.trb_client.backend.models.response.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import org.springframework.web.reactive.function.client.toEntityList
import java.util.Objects
import java.util.UUID
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class LoanRepository(
    private val webClient: WebClient
) {
    private val baseSubLoanUrl = "/api/v1/"

    fun getLoanTariffs(): List<TariffResponse> {
        val response =
            webClient.get().uri("${baseSubLoanUrl}tariff").exchangeToMono { it.toEntityList<TariffResponse>() }.retry(3).block()
        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body ?: throw Exception("Tariffs not found")
        }
        throw Exception("Tariffs not found")
    }

    fun getLoanRequestsByClient(clientId: String): List<LoanRequestResponse> {
        val response_UNDER_CONSIDERATION = webClient.get().uri {
            it.path("${baseSubLoanUrl}loan-applications/by-client")
                .queryParam("clientId", clientId)
                .queryParam("loanApplicationState", "UNDER_CONSIDERATION")
                .build()
        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.retry(3).block()

//        val response_APPROVED = webClient.get().uri {
//            it.path("${baseSubLoanUrl}loan-applications/by-client")
//                .queryParam("clientId", clientId)
//                .queryParam("loanApplicationState", "APPROVED")
//                .build()
//        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.retry(3).block()

        val response_REJECTED = webClient.get().uri {
            it.path("${baseSubLoanUrl}loan-applications/by-client")
                .queryParam("clientId", clientId)
                .queryParam("loanApplicationState", "REJECTED")
                .build()
        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.retry(3).block()

        val response_FAILED = webClient.get().uri {
            it.path("${baseSubLoanUrl}loan-applications/by-client")
                .queryParam("clientId", clientId)
                .queryParam("loanApplicationState", "FAILED")
                .build()
        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.retry(3).block()

        val allResponses = (
                (response_UNDER_CONSIDERATION?.body ?: listOf()) +
                        //(response_APPROVED?.body ?: listOf()) +
                        (response_REJECTED?.body ?: listOf()) +
                        (response_FAILED?.body ?: listOf())).sortedBy { it.creationDate }


        if (response_UNDER_CONSIDERATION?.statusCode?.is2xxSuccessful == true) {
            return allResponses
        }
        throw Exception("LoanRequests not found")
    }

    fun getLoansByClient(clientId: String): List<ShortLoanInfo> {
        val response = webClient.get().uri {
            it.path("${baseSubLoanUrl}client-loans")
                .queryParam("clientId", clientId)
                .build()
        }.exchangeToMono { it.toEntityList<ShortLoanInfo>() }.retry(3).block()
        println("LOANS -> $response")
        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body ?: throw Exception("Loans not found")
        }
        throw Exception("Loans not found")
    }

    fun getLastCreditRating(clientId: String): CreditRatingDto{
        val response = webClient.get().uri {
            it.path("${baseSubLoanUrl}credit-ratings/last")
                .queryParam("clientId", clientId)
                .build()
        }.exchangeToMono { it.toEntity<CreditRatingDto>() }.retry(3).block()
        println("Last credit rating -> ${response?.body}")
        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body ?: throw Exception("Loans not found")
        }
        throw Exception("Loans not found")
    }

    fun getLoanById(loanId: String): LoanResponse {
        val response = webClient.get().uri("${baseSubLoanUrl}loan/$loanId").exchangeToMono { it.toEntity<LoanResponse>() }.retry(3).block()

        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body ?: throw Exception("Loan not found")
        }
        throw Exception("Loan not found")
    }

    fun createLoan(clientId: String, tariffId: String, loanTermInDays: Int, issuedAmount: Double, currency: Currency): LoanRequestResponse {
        val request =
            LoanRequest(UUID.fromString(clientId), UUID.fromString(tariffId), loanTermInDays, issuedAmount, currency)

        val response = webClient.post().uri("${baseSubLoanUrl}loan-applications").bodyValue(request)
            .exchangeToMono {
                println(it as Any)
                println(it.toEntity<Any>().toString())
                it.toEntity<LoanRequestResponse>()
            }.retry(3).block()

        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body ?: throw Exception("Loan not created")
        }
        throw Exception("Loan not created")
    }
}
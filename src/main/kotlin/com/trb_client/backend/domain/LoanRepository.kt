package com.trb_client.backend.domain

import com.trb_client.backend.models.request.Currency
import com.trb_client.backend.models.request.LoanRequest
import com.trb_client.backend.models.response.LoanRequestResponse
import com.trb_client.backend.models.response.LoanResponse
import com.trb_client.backend.models.response.ShortLoanInfo
import com.trb_client.backend.models.response.TariffResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import org.springframework.web.reactive.function.client.toEntityList
import java.util.UUID

class LoanRepository(
    private val webClient: WebClient
) {
    private val baseSubLoanUrl = "/api/v1/"

    fun getLoanTariffs(): List<TariffResponse> {
        val response =
            webClient.get().uri("${baseSubLoanUrl}tariff").exchangeToMono { it.toEntityList<TariffResponse>() }.block()
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
        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.block()

//        val response_APPROVED = webClient.get().uri {
//            it.path("${baseSubLoanUrl}loan-applications/by-client")
//                .queryParam("clientId", clientId)
//                .queryParam("loanApplicationState", "APPROVED")
//                .build()
//        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.block()

        val response_REJECTED = webClient.get().uri {
            it.path("${baseSubLoanUrl}loan-applications/by-client")
                .queryParam("clientId", clientId)
                .queryParam("loanApplicationState", "REJECTED")
                .build()
        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.block()

        val response_FAILED = webClient.get().uri {
            it.path("${baseSubLoanUrl}loan-applications/by-client")
                .queryParam("clientId", clientId)
                .queryParam("loanApplicationState", "FAILED")
                .build()
        }.exchangeToMono { it.toEntityList<LoanRequestResponse>() }.block()

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
        }.exchangeToMono { it.toEntityList<ShortLoanInfo>() }.block()
        
        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body ?: throw Exception("Loans not found")
        }
        throw Exception("Loans not found")
    }

    fun getLoanById(loanId: String): LoanResponse {
        val response = webClient.get().uri("${baseSubLoanUrl}loan/$loanId").exchangeToMono { it.toEntity<LoanResponse>() }.block()

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
                it.toEntity<LoanRequestResponse>()
            }.block()

        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body ?: throw Exception("Loan not created")
        }
        throw Exception("Loan not created")
    }
}
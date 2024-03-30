package com.trb_client.backend.domain

import com.trb_client.backend.domain.models.AccountDto
import org.springframework.web.reactive.function.client.WebClient

class HiddenAccountRepository(

) {
    private val webClient = WebClient.builder().baseUrl("http://77.106.105.103:8085/").build()
    private val baseSubPrefsUrl = "api/preferences/"

    fun getHiddenAccounts(userToken: String): List<String> {
        val response =
            webClient.get().uri {
                it.path("${baseSubPrefsUrl}hidden-accounts")
                    .queryParam("Token", userToken)
                    .build()
            }.exchangeToMono { it.toEntityList(String::class.java) }.block()?.body

        return response ?: emptyList()
    }

    fun hideAccount(userToken: String, accountId: String) {
        val body = AccountDto(userToken, accountId)
        webClient.post().uri("${baseSubPrefsUrl}hide-account").bodyValue(body)
            .retrieve().bodyToMono(Object::class.java).block()
    }

    fun showAccount(userToken: String, accountId: String) {
        val body = AccountDto(userToken, accountId)

        webClient.post().uri("${baseSubPrefsUrl}show-account").bodyValue(body)
            .retrieve().bodyToMono(Object::class.java).block()
    }
}
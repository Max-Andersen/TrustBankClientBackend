package com.trb_client.backend.domain

import com.trb_client.backend.models.request.UserCredentials
import com.trb_client.backend.models.response.ClientInfo
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity
import java.util.UUID

class UserRepository(
    private val webClient: WebClient
) {
    private val baseSubUserUrl = "/api/v1/users/"

    fun getClientById(id: String): ClientInfo? {
        val response = webClient.get().uri {
            it.path("${baseSubUserUrl}client-info")
                .queryParam("clientId", UUID.fromString(id))
                .build()
        }.exchangeToMono { it.toEntity<ClientInfo>() }.retry(3).block()

        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body
        }
        throw Exception("User not found")
    }

    fun login(login: String, password: String): ClientInfo? {

        val requestModel = UserCredentials(login, password)

        val response = webClient.post().uri("${baseSubUserUrl}ident-client").bodyValue(requestModel)
            .exchangeToMono { it.toEntity<UUID>() }.retry(3).block()


        if (response?.statusCode?.is2xxSuccessful == true) {
            return getClientById(response.body!!.toString())
        }
        throw Exception("Login error")
    }

}
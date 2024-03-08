package com.trb_client.backend.domain

import com.trb_client.backend.models.response.ClientInfo
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.toEntity

class UserRequestRepository(
    private val webClient: WebClient
) {
    private val baseSubUserUrl = "/api/v1/users/"

    fun getClientById(id: String): ClientInfo? {
        val response = webClient.get().uri("$baseSubUserUrl$id").exchangeToMono { it.toEntity<ClientInfo>() }.block()

        if (response?.statusCode?.is2xxSuccessful == true) {
            return response.body
        }
        throw Exception("User not found")
    }

    fun login(login: String, password: String): ClientInfo? {
        // TODO ЖЁСТКОЕ, СДЕЛАТЬ КАК ПОЯВИТСЯ ЭНДПОИНТ
        val response = webClient.get().uri("$baseSubUserUrl/login?login=$login&password=$password")
            .exchangeToMono { it.toEntity<String>() }.block()


        if (response?.statusCode?.is2xxSuccessful == true) {
            return getClientById(response.body!!)
        }
        throw Exception("Login error")
    }

}
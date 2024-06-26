package com.trb_client.backend.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.trb_client.backend.domain.models.ChangeThemeDto
import com.trb_client.backend.domain.models.ThemeDto
import org.springframework.web.reactive.function.client.*

class ThemeRepository(
    private val webClient: WebClient
) {
    private val baseSubPrefsUrl = "api/preferences/"

    fun getAppTheme(userToken: String): Boolean {
        val response =
            webClient.get().uri {
                it.path("${baseSubPrefsUrl}theme")
                    .queryParam("Token", userToken)
                    .build()
            }.exchangeToMono { it.toEntity(ThemeDto::class.java) }.retry(3).block()?.body

        return response?.themeDark ?: false
    }

    fun changeAppTheme(userToken: String, isThemeDark: Boolean) {
        val body = ChangeThemeDto(userToken, isThemeDark)
        webClient.put().uri("${baseSubPrefsUrl}theme").bodyValue(body)
            .retrieve().bodyToMono<ThemeDto>().retry(3).block()
    }
}

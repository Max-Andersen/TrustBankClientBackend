package com.trb_client.backend.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

data class ChangeThemeDto(
    @JsonProperty("token") val token: String = "",
    @JsonProperty("themeDark") val themeDark: Boolean? = null
)

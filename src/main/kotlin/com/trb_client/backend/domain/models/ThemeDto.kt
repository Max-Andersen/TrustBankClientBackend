package com.trb_client.backend.domain.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class ThemeDto(
    @JsonProperty("themeDark") val themeDark: Boolean = false
)
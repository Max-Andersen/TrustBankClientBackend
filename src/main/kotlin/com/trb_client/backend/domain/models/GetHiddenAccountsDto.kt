package com.trb_client.backend.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

data class GetHiddenAccountsDto(
    @JsonProperty("token") val token: String
)

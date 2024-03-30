package com.trb_client.backend.domain.models

import com.fasterxml.jackson.annotation.JsonProperty

data class AccountDto(
    @JsonProperty("token") val token: String,
    @JsonProperty("accountId") val accountId: String
)
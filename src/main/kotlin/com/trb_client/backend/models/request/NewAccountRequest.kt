package com.trb_client.backend.models.request

import com.trb_client.backend.models.AccountType
import java.util.*


data class NewAccountRequest(
    val type: AccountType? = null,
    val clientFullName: String? = null,
    val externalClientId: UUID? = null
)

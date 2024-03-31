package com.trb_client.backend.models.response

import com.trb_client.backend.models.AccountType
import com.trb_client.backend.models.request.Currency
import java.util.*


data class AccountResponse(
    val id: UUID? = null,
    val type: AccountType,
    val balance: Double = 0.0,
    val clientFullName: String? = null,
    val externalClientId: String? = null,
    val creationDate: Date? = null,
    val closingDate: Date? = null,
    val isClosed: Boolean? = null,
    val currency: Currency
)

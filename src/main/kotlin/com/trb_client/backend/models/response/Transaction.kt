package com.trb_client.backend.models.response

import com.trb_client.backend.models.request.Currency
import java.util.*

data class Transaction(
    var id: UUID? = null,
    val externalId: UUID? = null,
    val date: Date? = null,
    val payerAccountId: UUID? = null,
    val payeeAccountId: UUID? = null,
    val amount: Double = .0,
    val currency: Currency,
    val type: TransactionType,
)

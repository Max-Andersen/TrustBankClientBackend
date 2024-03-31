package com.trb_client.backend.kafka.produce

import com.trb_client.backend.models.request.Currency
import com.trb_client.backend.models.response.TransactionType
import java.util.UUID

data class TransactionInit(
    val payerAccountId: UUID?,
    val payeeAccountId: UUID?,
    val amount: Double,
    val currency: Currency,
    val type: TransactionType
)
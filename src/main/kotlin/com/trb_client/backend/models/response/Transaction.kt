package com.trb_client.backend.models.response

import java.util.*

data class Transaction(
    var id: UUID? = null,
    val date: Date? = null,
    val payerAccountId: UUID? = null,
    val payeeAccountId: UUID? = null,
    val amount: Long = 0,
    val type: TransactionType,
    val state: TransactionState,
    val code: TransactionCode
)

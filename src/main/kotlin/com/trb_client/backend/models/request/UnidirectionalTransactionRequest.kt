package com.trb_client.backend.models.request

import java.util.*


data class UnidirectionalTransactionRequest(
    val accountId: UUID? = null,
    val amount: Long = 0
)


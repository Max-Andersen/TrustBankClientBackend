package com.trb_client.backend.models.request

import java.util.*

data class LoanRequest(
    var clientId: UUID,
    val tariffId: UUID,
    val loanTermInDays: Int = 0,
    val issuedAmount: Long = 0
)
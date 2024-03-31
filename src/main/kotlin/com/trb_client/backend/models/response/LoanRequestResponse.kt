package com.trb_client.backend.models.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.trb_client.backend.models.LoanRequestState
import java.util.*


data class LoanRequestResponse (
    val id: UUID? = null,
    val creationDate: Date? = null,
    val updatedDateFinal: Date? = null,
    val loanTermInDays: Long,
    val issuedAmount: Double,
    val clientId: UUID? = null,
    val officerId: UUID? = null,
    val state: LoanRequestState? = null,
    val tariff: TariffResponse,
)

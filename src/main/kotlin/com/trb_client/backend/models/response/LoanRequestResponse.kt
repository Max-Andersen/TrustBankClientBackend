package com.trb_client.backend.models.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.trb_client.backend.models.LoanRequestState
import java.util.*


data class LoanRequestResponse (
    val id: UUID,
    val creationDate: Date,
    val updatedDateFinal: Date? = null,
    val loanTermInDays: Long,
    val issuedAmount: Long,
    val clientId: UUID,
    val officerId: UUID? = null,
    val state: LoanRequestState,
    val tariff: TariffResponse,
)

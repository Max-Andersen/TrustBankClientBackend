package com.trb_client.backend.models.response

import com.trb_client.backend.models.LoanState
import java.util.*


data class LoanResponse(
    val id: UUID? = null,
    val issuedDate: Date? = null,
    val repaymentDate: Date? = null,
    val issuedAmount: Long = 0,
    val amountLoan: Long = 0,
    val amountDebt: Long = 0,
    val accruedPenny: Long = 0,
    val loanTermInDays: Long = 0,
    val clientId: UUID? = null,
    val accountId: UUID? = null,
    val state: LoanState? = null,
    val tariff: TariffResponse,
    val repayments: List<LoanRepaymentResponse>? = null,
)

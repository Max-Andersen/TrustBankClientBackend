package com.trb_client.backend.models.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.util.*

data class ShortLoanInfo(
    var id: UUID,
    val issuedDate: Date,
    val repaymentDate: Date,
    val amountDebt: Long = 0,
    val interestRate: BigDecimal
)

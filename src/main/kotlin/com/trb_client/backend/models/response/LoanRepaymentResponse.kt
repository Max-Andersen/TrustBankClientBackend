package com.trb_client.backend.models.response

import com.trb_client.backend.models.LoanRepaymentState
import java.util.*

data class LoanRepaymentResponse (
    val id: UUID? = null,
    val date: Date,
    val amount: Long = 0,
    val state: LoanRepaymentState,
)

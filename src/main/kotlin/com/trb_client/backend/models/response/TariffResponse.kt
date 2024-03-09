package com.trb_client.backend.models.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.util.*


data class TariffResponse (
    val id: UUID,
    val additionDate: Date,
    val name: String?,
    val description: String?,
    val interestRate: BigDecimal,
    val officerId: UUID
)

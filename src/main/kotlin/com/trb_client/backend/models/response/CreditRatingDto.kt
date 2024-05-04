package com.trb_client.backend.models.response

data class CreditRatingDto(
    val id: String,
    val clientId: String,
    val calculationDate: Long,
    val rating: Int
)

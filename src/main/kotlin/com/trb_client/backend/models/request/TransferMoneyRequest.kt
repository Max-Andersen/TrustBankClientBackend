package com.trb_client.backend.models.request

data class TransferMoneyRequest(
    val payerAccountId: String,
    val payeeAccountId: String,
    val amount: Long
)

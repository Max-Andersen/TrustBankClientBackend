package com.trb_client.backend.models.response

data class TransactionHistoryPage(
    val pageNumber: Int,
    val pageSize: Int,
    val elements: List<Transaction>
)
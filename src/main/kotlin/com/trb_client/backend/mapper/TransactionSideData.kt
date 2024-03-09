package com.trb_client.backend.mapper

import com.trb_client.backend.models.response.TransactionCode
import com.trb_client.backend.models.response.TransactionState
import com.trb_client.backend.models.response.TransactionType
import com.trustbank.client_mobile.proto.TransactionCode as TransactionCodeGrpc
import com.trustbank.client_mobile.proto.TransactionState as TransactionStateGrpc
import com.trustbank.client_mobile.proto.TransactionType as TransactionTypeGrpc

fun TransactionType.toGrpc() = TransactionTypeGrpc.valueOf(this.name)

fun TransactionState.toGrpc(): TransactionStateGrpc {
    return when (this) {
        TransactionState.DONE -> TransactionStateGrpc.TRANSACTION_DONE
        TransactionState.REJECTED -> TransactionStateGrpc.TRANSACTION_REJECTED
    }
}

fun TransactionCode.toGrpc() = TransactionCodeGrpc.valueOf(this.name)
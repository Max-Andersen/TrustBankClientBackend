package com.trb_client.backend.mapper

import com.google.protobuf.Timestamp
import com.trb_client.backend.models.response.Transaction
import com.trustbank.client_mobile.proto.Account
import com.trustbank.client_mobile.proto.Transaction as TransactionGrpc

fun Transaction.toGrpc(payee: Account?, payer: Account?): TransactionGrpc {
    val builder = TransactionGrpc.newBuilder()
        .setId(id.toString())
        .setDate(Timestamp.newBuilder().setSeconds(date?.time ?: 0))
        .setAmount(amount)
        .setType(type.toGrpc())
        .setCurrency(currency.toString())
    payee?.let { builder.setPayee(it) }
    payer?.let { builder.setPayer(it) }
    return builder.build()
}


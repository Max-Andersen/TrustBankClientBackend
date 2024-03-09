package com.trb_client.backend.mapper

import com.google.protobuf.Timestamp
import com.trb_client.backend.models.response.Transaction
import com.trustbank.client_mobile.proto.Account
import com.trustbank.client_mobile.proto.Transaction as TransactionGrpc

fun Transaction.toGrpc(payee: Account?, payer: Account?): TransactionGrpc = TransactionGrpc.newBuilder()
    .setId(id.toString())
    .setDate(Timestamp.newBuilder().setSeconds(date?.time ?: 0))
    .setPayer(payer)
    .setPayee(payee)
    .setAmount(amount)
    .setType(type.toGrpc())
    .setState(state.toGrpc())
    .setCode(code.toGrpc())
    .build()


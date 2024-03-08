package com.trb_client.backend.mapper

import com.google.protobuf.Timestamp
import com.trb_client.backend.models.response.AccountResponse
import com.trustbank.client_mobile.proto.Account
import com.trustbank.client_mobile.proto.Client

fun AccountResponse.toAccountGrpc(owner: Client) = Account.newBuilder()
    .setId(id.toString())
    .setCreationDate(
        Timestamp.newBuilder().setSeconds(creationDate?.time ?: 0)
    )
    .setClosingDate(
        Timestamp.newBuilder().setSeconds(closingDate?.time ?: 0)
    )
    .setOwner(owner)
    .setType(type.toAccountTypeGrpc())
    .setBalance(balance)
    .setOwnerFullName(clientFullName)
    .setIsBlocked(isClosed ?: false)
    .build()
package com.trb_client.backend.mapper

import com.google.protobuf.Timestamp
import com.trb_client.backend.models.response.TariffResponse
import com.trustbank.client_mobile.proto.LoanTariff

fun TariffResponse.toGrpc() = LoanTariff.newBuilder()
    .setId(id.toString())
    .setAdditionDate(Timestamp.newBuilder().setSeconds(additionDate.time))
    .setName(name)
    .setDescription(description)
    .setInterestRate(interestRate.toDouble())
    .build()
package com.trb_client.backend.mapper

import com.trb_client.backend.models.response.ClientInfo
import com.trustbank.client_mobile.proto.Client

fun ClientInfo.toGrpc(): Client = Client.newBuilder()
    .setId(id.toString())
    .setFirstName(firstName)
    .setLastName(lastName)
    .setPatronymic(patronymic)
    .setPhoneNumber(phoneNumber)
    .setAddress(address)
    .setPassportNumber(passportNumber)
    .setPassportSeries(passportSeries)
    .setIsBlocked(isBlocked).build()

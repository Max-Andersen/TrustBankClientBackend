package com.trb_client.backend.mapper

import com.trb_client.backend.models.AccountType
import com.trustbank.client_mobile.proto.AccountType as AccountTypeGrpc

fun AccountType.toAccountTypeGrpc() = AccountTypeGrpc.valueOf(this.name)
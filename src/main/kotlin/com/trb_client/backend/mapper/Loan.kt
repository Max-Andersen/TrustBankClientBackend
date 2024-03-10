package com.trb_client.backend.mapper

import com.google.protobuf.Timestamp
import com.trb_client.backend.models.LoanRepaymentState
import com.trb_client.backend.models.LoanRepaymentState.*
import com.trb_client.backend.models.LoanState
import com.trb_client.backend.models.response.LoanRepaymentResponse
import com.trb_client.backend.models.response.LoanRequestResponse
import com.trb_client.backend.models.response.LoanResponse
import com.trb_client.backend.models.response.ShortLoanInfo
import com.trustbank.client_mobile.proto.Loan
import com.trustbank.client_mobile.proto.LoanPaymentState
import com.trustbank.client_mobile.proto.LoanRequest
import com.trustbank.client_mobile.proto.LoanRequestState
import com.trustbank.client_mobile.proto.Payment
import com.trustbank.client_mobile.proto.LoanRepaymentState as LoanRepaymentStateGrpc
import com.trustbank.client_mobile.proto.LoanState as LoanStateGrpc
import com.trustbank.client_mobile.proto.ShortLoanInfo as ShortLoanInfoGrpc

fun LoanRequestResponse.toGrpc(): LoanRequest = LoanRequest.newBuilder()
    .setId(id.toString())
    .setCreationDate(Timestamp.newBuilder().setSeconds(creationDate.time))
    .setUpdatedDateFinal(Timestamp.newBuilder().setSeconds(updatedDateFinal?.time ?: 0))
    .setLoanTermInDays(loanTermInDays)
    .setIssuedAmount(issuedAmount)
    .setClientId(clientId.toString())
    .setOfficerId(officerId?.toString() ?: "")
    .setState(LoanRequestState.valueOf(state.name))
    .setTariff(tariff.toGrpc())
    .build()


fun ShortLoanInfo.toGrpc(): ShortLoanInfoGrpc = ShortLoanInfoGrpc.newBuilder()
    .setId(id.toString())
    .setIssuedDate(Timestamp.newBuilder().setSeconds(issuedDate.time))
    .setRepaymentDate(Timestamp.newBuilder().setSeconds(repaymentDate.time))
    .setAmountDebt(amountDebt)
    .setInterestRate(interestRate.toDouble())
    .build()


fun LoanResponse.toGrpc(): Loan = Loan.newBuilder()
    .setId(id.toString())
    .setIssuedDate(Timestamp.newBuilder().setSeconds(issuedDate?.time ?: 0))
    .setRepaymentDate(Timestamp.newBuilder().setSeconds(repaymentDate?.time ?: 0))
    .setIssuedAmount(issuedAmount)
    .setAmountLoan(amountLoan)
    .setAmountDebt(amountDebt)
    .setAccruedPenny(accruedPenny)
    .setLoanTermInDays(loanTermInDays)
    .setClientId(clientId.toString())
    .setAccountId(accountId.toString())
    .setState(state?.toGrpc())
    .setTariff(tariff.toGrpc())
    .addAllOperations(repayments?.map { it.toGrpc() } ?: emptyList())
    .build()

fun LoanState.toGrpc(): LoanStateGrpc = when (this) {
    LoanState.OPEN -> LoanStateGrpc.OPEN
    LoanState.CLOSED -> LoanStateGrpc.CLOSED
}

fun LoanRepaymentResponse.toGrpc(): Payment = Payment.newBuilder()
    .setId(id.toString())
    .setDate(Timestamp.newBuilder().setSeconds(date.time ?: 0))
    .setAmount(amount)
    .setState(state.toGrpc())
    .build()


fun LoanRepaymentState.toGrpc(): LoanRepaymentStateGrpc = when (this) {
    OPEN -> LoanRepaymentStateGrpc.PAYMENT_OPEN
    IN_PROGRESS -> LoanRepaymentStateGrpc.IN_PROGRESS
    DONE -> LoanRepaymentStateGrpc.DONE
    REJECTED -> LoanRepaymentStateGrpc.PAYMENT_REJECTED
}

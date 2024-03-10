package com.trb_client.backend

import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.LoanRepository
import com.trb_client.backend.mapper.toGrpc
import com.trustbank.client_mobile.proto.*
import io.grpc.stub.StreamObserver

class LoanOperationService(
    private val loanRepository: LoanRepository
) : LoanOperationServiceGrpc.LoanOperationServiceImplBase() {

    override fun getLoanTariffs(request: GetLoanTariffsRequest, responseObserver: StreamObserver<LoanTariff>) {
        val loanTariffs = loanRepository.getLoanTariffs()
        loanTariffs.forEach {
            responseObserver.onNext(it.toGrpc())
        }
        responseObserver.onCompleted()
    }

    override fun createLoanRequest(request: CreateLoanRequestRequest, responseObserver: StreamObserver<LoanRequest>) {
        val userId = UserAuthorizingData.id.get()
        val loanRequest =
            loanRepository.createLoan(userId, request.tariffId, request.loanTermInDays, request.issuedAmount)
        responseObserver.onNext(loanRequest.toGrpc())
        responseObserver.onCompleted()
    }

    override fun getLoanRequests(request: GetLoanRequestRequest, responseObserver: StreamObserver<LoanRequest>) {
        val userId = UserAuthorizingData.id.get()
        val loanRequests = loanRepository.getLoanRequestsByClient(userId)
        loanRequests.forEach {
            responseObserver.onNext(it.toGrpc())
        }
        responseObserver.onCompleted()
    }

    override fun getLoans(request: GetClientLoansRequest, responseObserver: StreamObserver<ShortLoanInfo>) {
        val userId = UserAuthorizingData.id.get()
        val loans = loanRepository.getLoansByClient(userId)
        loans.forEach {
            responseObserver.onNext(it.toGrpc())
        }
        responseObserver.onCompleted()
    }

    override fun getLoanById(request: GetLoanByIdRequest, responseObserver: StreamObserver<Loan>) {
        val loan = loanRepository.getLoanById(request.id)
        responseObserver.onNext(loan.toGrpc())
        responseObserver.onCompleted()
    }
}
package com.trb_client.backend.services

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.LoanRepository
import com.trb_client.backend.mapper.toGrpc
import com.trb_client.backend.models.request.Currency
import com.trustbank.client_mobile.proto.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.retry.annotation.CircuitBreaker

@GrpcService(interceptors = [HeaderServerInterceptor::class])
class LoanOperationService(
    private val loanRepository: LoanRepository
) : LoanOperationServiceGrpc.LoanOperationServiceImplBase() {
    @CircuitBreaker
    override fun getLoanTariffs(request: GetLoanTariffsRequest, responseObserver: StreamObserver<LoanTariff>) {
        try {
            val loanTariffs = loanRepository.getLoanTariffs()
            loanTariffs.forEach {
                responseObserver.onNext(it.toGrpc())
            }
            responseObserver.onCompleted()
        } catch (e: Exception){
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка получения тарифов").asRuntimeException())
        }

    }
    @CircuitBreaker
    override fun createLoanRequest(request: CreateLoanRequestRequest, responseObserver: StreamObserver<LoanRequest>) {
        try {
            val userId = UserAuthorizingData.id.get()
            val loanRequest =
                loanRepository.createLoan(userId, request.tariffId, request.loanTermInDays, request.issuedAmount, Currency.valueOf(request.currency))
            responseObserver.onNext(loanRequest.toGrpc())
            responseObserver.onCompleted()
        } catch (e: Exception){
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка создание запроса на кредит").asRuntimeException())
        }

    }
    @CircuitBreaker
    override fun getLoanRequests(request: GetLoanRequestRequest, responseObserver: StreamObserver<LoanRequest>) {
        try {
            val userId = UserAuthorizingData.id.get()
            val loanRequests = loanRepository.getLoanRequestsByClient(userId)
            loanRequests.forEach {
                responseObserver.onNext(it.toGrpc())
            }
            responseObserver.onCompleted()
        } catch (e: Exception){
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка получения запросов на кредиты").asRuntimeException())
        }
    }
    @CircuitBreaker
    override fun getLoans(request: GetClientLoansRequest, responseObserver: StreamObserver<ShortLoanInfo>) {
        try {
            val userId = UserAuthorizingData.id.get()
            val loans = loanRepository.getLoansByClient(userId)
            loans.forEach {
                responseObserver.onNext(it.toGrpc())
            }
            responseObserver.onCompleted()

        } catch (e: Exception){
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка получения кредитов").asRuntimeException())
        }
    }
    @CircuitBreaker
    override fun getLoanById(request: GetLoanByIdRequest, responseObserver: StreamObserver<Loan>) {
        try {
            val loan = loanRepository.getLoanById(request.id)
            println("LOAN ACC  _> $loan")
            println("\n\nLOAN grpc  _> ${loan.toGrpc()} \n\n")
            responseObserver.onNext(loan.toGrpc())
            responseObserver.onCompleted()

        } catch (e: Exception){
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка получения детальной информации о кредите").asRuntimeException())
        }
    }
    @CircuitBreaker
    override fun getLastCreditRating(
        request: RequestLastCreditRating,
        responseObserver: StreamObserver<CreditRating>
    ) {
        try {
            val userId = UserAuthorizingData.id.get()
            val rating = try {
                loanRepository.getLastCreditRating(userId).rating
            } catch (e: Exception){
                -1
            }
            responseObserver.onNext(CreditRating.newBuilder().setRating(rating).build())
            responseObserver.onCompleted()
        } catch (e: Exception){
            responseObserver.onError(Status.INTERNAL.withDescription("Ошибка получения кредитного рейтинга").asRuntimeException())
        }

    }
}
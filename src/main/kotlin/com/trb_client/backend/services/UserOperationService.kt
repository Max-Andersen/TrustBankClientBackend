package com.trb_client.backend.services

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.UserRepository
import com.trb_client.backend.mapper.toGrpc
import com.trustbank.client_mobile.proto.Client
import com.trustbank.client_mobile.proto.ClientRequest
import com.trustbank.client_mobile.proto.LoginRequest
import com.trustbank.client_mobile.proto.UserOperationServiceGrpc
import io.grpc.Status
import io.grpc.Status.UNAUTHENTICATED
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.retry.annotation.CircuitBreaker
import org.springframework.retry.annotation.Recover

@GrpcService(interceptors = [HeaderServerInterceptor::class])
class UserOperationService(
    private val userRepository: UserRepository
) : UserOperationServiceGrpc.UserOperationServiceImplBase() {
    @CircuitBreaker
    override fun login(request: LoginRequest, responseObserver: StreamObserver<Client>) {
        val clientInfo = userRepository.login(request.login, request.password)
        clientInfo?.let {
            val client = it.toGrpc()
            responseObserver.onNext(client)
            responseObserver.onCompleted()
        } ?: responseObserver.onError(UNAUTHENTICATED.asRuntimeException())
    }
    @CircuitBreaker
    override fun getClientById(request: ClientRequest, responseObserver: StreamObserver<Client>) {
        try {
            val userId = UserAuthorizingData.id.get()
            val clientInfo = userRepository.getClientById(userId)
            clientInfo?.let {
                val client = it.toGrpc()
                responseObserver.onNext(client)
                responseObserver.onCompleted()
            } ?: responseObserver.onError(UNAUTHENTICATED.asRuntimeException())
        } catch (e: Exception){
            responseObserver.onError(
                Status.INTERNAL.withDescription("Ошибка получения информации о пользователе").asRuntimeException()
            )
        }
    }

    @Recover
    fun rec(){

    }
}
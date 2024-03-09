package com.trb_client.backend

import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.UserRepository
import com.trb_client.backend.mapper.toGrpc
import com.trustbank.client_mobile.proto.Client
import com.trustbank.client_mobile.proto.ClientRequest
import com.trustbank.client_mobile.proto.LoginRequest
import com.trustbank.client_mobile.proto.UserOperationServiceGrpc
import io.grpc.Status.UNAUTHENTICATED
import io.grpc.stub.StreamObserver

class UserOperationService(
    private val userRepository: UserRepository
) : UserOperationServiceGrpc.UserOperationServiceImplBase() {
    override fun login(request: LoginRequest, responseObserver: StreamObserver<Client>) {
        val clientInfo = userRepository.login(request.login, request.password)
        clientInfo?.let {
            val client = it.toGrpc()
            responseObserver.onNext(client)
            responseObserver.onCompleted()
        } ?: responseObserver.onError(UNAUTHENTICATED.asRuntimeException())
    }

    override fun getClientById(request: ClientRequest, responseObserver: StreamObserver<Client>) {
        val userId = UserAuthorizingData.id.get()
        val clientInfo = userRepository.getClientById(userId)
        clientInfo?.let {
            val client = it.toGrpc()
            responseObserver.onNext(client)
            responseObserver.onCompleted()
        } ?: responseObserver.onError(UNAUTHENTICATED.asRuntimeException())
    }
}
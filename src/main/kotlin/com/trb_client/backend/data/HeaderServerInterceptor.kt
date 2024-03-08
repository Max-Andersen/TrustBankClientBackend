package com.trb_client.backend.data

import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Metadata as GrpcMetadata


class HeaderServerInterceptor : ServerInterceptor {

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>?,
        requestHeaders: GrpcMetadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {

        val clientId = requestHeaders.get(GrpcMetadata.Key.of("client_id_header", GrpcMetadata.ASCII_STRING_MARSHALLER)).toString()
        UserAuthorizingData.id.set(clientId)

        return next.startCall(call, requestHeaders)
    }
}
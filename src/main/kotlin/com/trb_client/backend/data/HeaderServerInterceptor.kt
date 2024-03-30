package com.trb_client.backend.data

import com.google.firebase.auth.FirebaseAuth
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

        val clientToken = requestHeaders.get(GrpcMetadata.Key.of("client_id_header", GrpcMetadata.ASCII_STRING_MARSHALLER)).toString()

        val userUid = FirebaseAuth.getInstance().verifyIdToken(clientToken).uid

        UserAuthorizingData.id.set(userUid)
        UserAuthorizingData.firebaseToken.set(clientToken)

        return next.startCall(call, requestHeaders)
    }
}
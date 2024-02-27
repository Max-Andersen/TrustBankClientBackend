package com.trb_client.backend

import com.google.protobuf.Int64Value
import com.trustbank.client_mobile.proto.AccountOperationsServiceGrpcKt
import com.trustbank.client_mobile.proto.HelloRequest
import com.trustbank.client_mobile.proto.HelloResponse
import io.grpc.Server
import io.grpc.ServerBuilder
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.boot.autoconfigure.SpringBootApplication

@GrpcService
class Service: AccountOperationsServiceGrpcKt.AccountOperationsServiceCoroutineImplBase() {

    override suspend fun helloWorld(request: HelloRequest): HelloResponse {

        return HelloResponse.newBuilder().setMessage("Hello ${request.name}").build()
    }

}



class AccountOperationsServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(Service())
        .build()

    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@AccountOperationsServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}

fun main() {
    val port = 50051
    val server = AccountOperationsServer(port)
    server.start()
    server.blockUntilShutdown()
}
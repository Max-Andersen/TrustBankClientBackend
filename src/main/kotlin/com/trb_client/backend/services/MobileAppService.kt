package com.trb_client.backend.services

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.HiddenAccountRepository
import com.trb_client.backend.domain.ThemeRepository
import com.trustbank.client_mobile.proto.*
import io.grpc.Status
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.retry.annotation.CircuitBreaker

@GrpcService(interceptors = [HeaderServerInterceptor::class])
class MobileAppService(
    private val themeRepository: ThemeRepository,
    private val hidedAccountRepository: HiddenAccountRepository
) : MobileAppServiceGrpc.MobileAppServiceImplBase() {
    @CircuitBreaker
    override fun getAppTheme(request: GetAppThemeRequest, responseObserver: StreamObserver<MobileTheme>) {
        try {

            val userId = UserAuthorizingData.firebaseToken.get()

            val isThemeDark = themeRepository.getAppTheme(userId)

            responseObserver.onNext(
                MobileTheme.newBuilder().setTheme(if (isThemeDark) Theme.DARK else Theme.LIGHT).build()
            )
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Ошибка получения темы приложения").asRuntimeException()
            )
        }
    }
    @CircuitBreaker
    override fun changeMobileTheme(request: MobileTheme, responseObserver: StreamObserver<MobileTheme>) {
        try {

            val userId = UserAuthorizingData.firebaseToken.get()
            themeRepository.changeAppTheme(userId, request.theme == Theme.DARK)
            println("Theme changed to ${request.theme}")
//        val isThemeDark = themeRepository.getAppTheme(userId)

//        if (theme != null) {
//            var newTheme = UserTheme(null, UUID.fromString(userId), request.theme == Theme.DARK)
//            themeRepository.save(newTheme)
//
//            responseObserver.onNext(
//                MobileTheme.newBuilder().setTheme(if (theme.isThemeDark) Theme.DARK else Theme.LIGHT).build()
//            )
//        } else {
//            val newTheme = UserTheme(null, UUID.fromString(userId), request.theme == Theme.DARK)
//
//            themeRepository.save(newTheme)

            responseObserver.onNext(MobileTheme.newBuilder().setTheme(Theme.LIGHT).build())
//        }
            responseObserver.onCompleted()
        } catch (e: Exception) {
            responseObserver.onError(
                Status.INTERNAL.withDescription("Ошибка получения смены темы приложения").asRuntimeException()
            )
        }
    }

    @CircuitBreaker
    override fun showAccount(request: AccountId, responseObserver: StreamObserver<EmptyResponse>) {
        val userId = UserAuthorizingData.firebaseToken.get()

        hidedAccountRepository.showAccount(userId, request.id)

        responseObserver.onNext(EmptyResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }
    @CircuitBreaker
    override fun hideAccount(request: AccountId, responseObserver: StreamObserver<EmptyResponse>) {
        val userId = UserAuthorizingData.firebaseToken.get()

        hidedAccountRepository.hideAccount(userId, request.id)

        responseObserver.onNext(EmptyResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }
}
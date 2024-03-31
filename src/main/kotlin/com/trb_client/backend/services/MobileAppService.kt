package com.trb_client.backend.services

import com.trb_client.backend.data.HeaderServerInterceptor
import com.trb_client.backend.data.UserAuthorizingData
import com.trb_client.backend.domain.HiddenAccountRepository
import com.trb_client.backend.domain.ThemeRepository
import com.trustbank.client_mobile.proto.*
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService(interceptors = [HeaderServerInterceptor::class])
class MobileAppService(
    private val themeRepository: ThemeRepository,
    private val hidedAccountRepository: HiddenAccountRepository
) : MobileAppServiceGrpc.MobileAppServiceImplBase() {

    override fun getAppTheme(request: GetAppThemeRequest, responseObserver: StreamObserver<MobileTheme>) {
        val userId = UserAuthorizingData.firebaseToken.get()

        val isThemeDark = themeRepository.getAppTheme(userId)

        responseObserver.onNext(
            MobileTheme.newBuilder().setTheme(if (isThemeDark) Theme.DARK else Theme.LIGHT).build()
        )
        responseObserver.onCompleted()

    }

    override fun changeMobileTheme(request: MobileTheme, responseObserver: StreamObserver<MobileTheme>) {
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
    }



    override fun showAccount(request: AccountId, responseObserver: StreamObserver<EmptyResponse>) {
        val userId = UserAuthorizingData.firebaseToken.get()

        hidedAccountRepository.showAccount(userId, request.id)

        responseObserver.onNext(EmptyResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }
    override fun hideAccount(request: AccountId, responseObserver: StreamObserver<EmptyResponse>) {
        val userId = UserAuthorizingData.firebaseToken.get()

        hidedAccountRepository.hideAccount(userId, request.id)

        responseObserver.onNext(EmptyResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }
}
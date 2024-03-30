package com.trb_client.backend.interceptor

import com.trb_client.backend.data.HeaderServerInterceptor
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor
import org.springframework.context.annotation.Configuration

@Configuration(proxyBeanMethods = false)
class GlobalInterceptorConfiguration {

    @GrpcGlobalServerInterceptor
    fun logServerInterceptor(): HeaderServerInterceptor? = HeaderServerInterceptor()
}
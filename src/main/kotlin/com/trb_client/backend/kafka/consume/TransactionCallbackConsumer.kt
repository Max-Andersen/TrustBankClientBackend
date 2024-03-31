package com.trb_client.backend.kafka.consume

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.trb_client.backend.models.response.Transaction
import kotlinx.coroutines.flow.MutableSharedFlow
import org.apache.kafka.common.requests.FetchMetadata.log
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*


@Service
class TransactionCallbackConsumer(
    private val objectMapper: ObjectMapper
) {
    val flow = MutableSharedFlow<Transaction>()
    init {
        consume("", "")
    }

    @KafkaListener(topics = [TOPIC], groupId = GROUP_ID, id = "123", autoStartup = "true")
    fun consume(
        @Header(KafkaHeaders.RECEIVED_KEY) key: String?,
        @Payload message: String?
    ) {
        log.info("Transaction get")

        try {
            println("CONSUME GET     " + message.toString())
            val transactionCallback: Transaction? = objectMapper.readValue(message, Transaction::class.java)
            val externalTransactionId = objectMapper.readValue(key, UUID::class.java)
//            if (transactionCallback != null) {
//                runBlocking {
//                    flow.emit(transactionCallback)
//                }
//            }

            log.info("Transaction finished success")
        } catch (exception: JsonProcessingException) {
            println("CONSUME ERROR")
//            throw Exception(
//                exception
//            )
        }
    }

    companion object {
        private const val TOPIC = "transaction.callback"
        private const val GROUP_ID = "my-group"
    }
}

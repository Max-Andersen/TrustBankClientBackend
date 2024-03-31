package com.trb_client.backend.kafka.produce

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.*


@Service
class TransactionInitProducer(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
    private val objectMapper: ObjectMapper
) {


    private val topic: String = "transaction.initialization"
    fun sendMessage(externalTransactionId: UUID?, transactionInit: TransactionInit?) {
        try {
            val key = objectMapper.writeValueAsString(externalTransactionId)
            val value = objectMapper.writeValueAsString(transactionInit)
            println(topic)
            println(TopicBuilder.name(topic).build().toString())
            println(key)
            println(value)
            kafkaTemplate.send(topic, key, value)
        } catch (exception: JsonProcessingException) {
            throw Exception(exception)
        }
    }
}
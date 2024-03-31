package com.trb_client.backend.kafka.produce

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.stereotype.Component

@Configuration
class KafkaProducerConfig {

//    @Value("\${kafka}")
//    private lateinit var bootstrapServers: String
//
//    @Bean
//    fun producerFactory(): ProducerFactory<Any, Any> {
//        val configProps = HashMap<String, Any>()
//        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
//        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        configProps[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
//
//        return DefaultKafkaProducerFactory(configProps)
//    }
//    @Bean
//    fun consumerFactory(): ConsumerFactory<Any, Any> {
//        val configProps = HashMap<String, Any>()
//        configProps[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
//        configProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//        configProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
//
//        return DefaultKafkaConsumerFactory(configProps)
//    }
//
//    @Bean
//    fun kafkaTemplate(): KafkaTemplate<Any, Any> {
//        return KafkaTemplate(producerFactory())
//    }
//
//    @Bean
//    fun topicCreate(): NewTopic {
//        return TopicBuilder.name("transaction.initialization").build()
//    }
//    @Bean
//    fun callbackCreate(): NewTopic {
//        return TopicBuilder.name("transaction.callback").build()
//    }
}
//@Component
//class BeanLogger(private val applicationContext: ApplicationContext) : CommandLineRunner {
//
//    override fun run(vararg args: String?) {
//        val beanNames = applicationContext.beanDefinitionNames
//        beanNames.sort()
//
//        for (beanName in beanNames) {
//            println("Bean Name: $beanName")
//        }
//    }
//}
package com.alexander.bryksin.kotlinspringcleanarchitecture.api.configuration.kafka

data class KafkaTopicData(var name: String = "", var partitions: Int = 1, var replication: Int = 1)

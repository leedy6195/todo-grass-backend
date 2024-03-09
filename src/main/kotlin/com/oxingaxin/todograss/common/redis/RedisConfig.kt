package com.oxingaxin.todograss.common.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableRedisRepositories
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private var port: Int? = 0

    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(host, port!!)

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> =
        RedisTemplate<String, String>().apply {
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            connectionFactory = redisConnectionFactory()
        }
}
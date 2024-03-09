package com.oxingaxin.todograss.common.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class RedisDao(
    val redisTemplate: RedisTemplate<String, String>
) {

    fun setValue(key: String, data: String) {
        val ops = redisTemplate.opsForValue();
        ops.set(key, data)
    }

    fun setValue(key: String, data: String, duration: Duration) {
        val ops = redisTemplate.opsForValue()
        ops.set(key, data, duration)
    }

    fun getValue(key: String) =
        redisTemplate.opsForValue().get(key)

    fun deleteValue(key: String) {
        redisTemplate.delete(key)
    }


}
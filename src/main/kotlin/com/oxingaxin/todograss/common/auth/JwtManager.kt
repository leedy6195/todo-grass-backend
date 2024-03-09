package com.oxingaxin.todograss.common.auth

import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.common.dto.TokenInfo
import com.oxingaxin.todograss.common.dto.TokenType
import com.oxingaxin.todograss.common.redis.RedisDao
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.util.*
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.time.Duration


@Component
class JwtManager() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${jwt.secret}")
    lateinit var secretKey: String
    private val key by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)) }

    @Value("\${jwt.expiration-millis.access-token}")
    var accessTokenExpMillis: Long = 0L

    @Value("\${jwt.expiration-millis.refresh-token}")
    var refreshTokenExpMillis: Long = 0L


    fun generateAccessToken(authentication: Authentication) : TokenInfo {
        val authorities: String = authentication.authorities.joinToString(",") { it.authority }
        val now = Date()
        val expMillis = accessTokenExpMillis
        val expiration = Date(now.time + expMillis)

        val token = Jwts.builder()
            .setSubject(authentication.name)
            .claim("type", TokenType.ACCESS.name)
            .claim("auth", authorities)
            .claim("userId", (authentication.principal as CustomUser).userId)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        return TokenInfo(TokenType.ACCESS, token)
    }

    fun generateRefreshToken(authentication: Authentication) : TokenInfo {
        val now = Date()
        val expMillis = refreshTokenExpMillis
        val expiration = Date(now.time + expMillis)

        val token = Jwts.builder()
            .claim("type", TokenType.REFRESH.name)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()

        return TokenInfo(TokenType.REFRESH, token)
    }

    fun getAuthentication(token: String): Authentication {
        val claims: Claims = getClaims(token)

        val auth = claims["auth"] ?: throw RuntimeException("Invalid token")
        val userId = claims["userId"] ?: throw RuntimeException("Invalid token")

        val authorities = (auth as String).split(",").map { SimpleGrantedAuthority(it) }
        val principal: UserDetails = CustomUser(userId.toString().toLong(), claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }

    fun getClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body

    fun validateToken(token: String): Boolean {
        try {
            getClaims(token)
            return true
        } catch (e: Exception) {
            when (e) {
                is SecurityException -> logger.error("Invalid JWT token")
                is ExpiredJwtException -> logger.error("Expired JWT token")
                is MalformedJwtException -> logger.error("Invalid JWT token")
                is UnsupportedJwtException -> logger.error("Unsupported JWT token")
                is IllegalArgumentException -> logger.error("JWT claims string is empty")
                else -> logger.error("Unknown error occurred")
            }
        }
        return false
    }
}
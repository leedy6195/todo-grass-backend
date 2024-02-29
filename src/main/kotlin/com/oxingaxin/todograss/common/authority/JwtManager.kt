package com.oxingaxin.todograss.common.authority

import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.common.dto.TokenInfo
import com.oxingaxin.todograss.common.dto.TokenType
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.util.*
import io.jsonwebtoken.security.SecurityException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

const val ACCESS_TOKEN_EXP_MILLIS = 1000L * 60 * 3
const val REFRESH_TOKEN_EXP_MILLIS = 1000L * 60 * 10

@Component
class JwtManager {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${jwt.secret}")
    lateinit var secretKey: String
    private val key by lazy { Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)) }


    fun generateToken(authentication: Authentication, tokenType: TokenType): TokenInfo {
        val authorities: String = authentication.authorities.joinToString(",") { it.authority }

        val now = Date()
        val expireMillis = if (tokenType == TokenType.ACCESS) ACCESS_TOKEN_EXP_MILLIS else REFRESH_TOKEN_EXP_MILLIS
        val expiration = Date(now.time + expireMillis)

        val token = Jwts.builder()
            .setSubject(authentication.name)
            .claim("type", tokenType.name)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(key, SignatureAlgorithm.HS256)
            .claim("auth", authorities)
            .claim("userId", (authentication.principal as CustomUser).userId)
            .compact()

        return TokenInfo(tokenType, token)
    }

    fun getAuthentication(token: String): Authentication {
        val claims: Claims = getClaims(token)

        val auth = claims["auth"] ?: throw RuntimeException("Invalid token")
        val userId = claims["userId"] ?: throw RuntimeException("Invalid token")

        val authorities = (auth as String).split(",").map { SimpleGrantedAuthority(it) }
        val principal: UserDetails = CustomUser(userId.toString().toLong(), claims.subject, "", authorities)

        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }

    private fun getClaims(token: String): Claims =
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

    fun doesTokenExpireSoon(token: String): Boolean {
        val claims: Claims = getClaims(token)
        val expiration = claims.expiration.time
        val now = Date().time
        return expiration - now < 1000 * 60 * 1
    }
}
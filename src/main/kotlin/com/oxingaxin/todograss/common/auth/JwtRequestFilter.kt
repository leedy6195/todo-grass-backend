package com.oxingaxin.todograss.common.auth

import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.common.dto.TokenType
import com.oxingaxin.todograss.common.redis.RedisDao
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtRequestFilter(
    private val jwtManager: JwtManager,
    private val redisDao: RedisDao
) : OncePerRequestFilter() {

    @Value("\${jwt.expiration-millis.access-token}")
    var accessTokenExpMillis: Long = 0L

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        val token = resolveToken(request)
        try {
            if (token != null && jwtManager.validateToken(token)) {

                val authentication = jwtManager.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication

            }
            /* access token expires without signout */
            else if (SecurityContextHolder.getContext()?.authentication != null &&
                redisDao.getValue((SecurityContextHolder.getContext().authentication?.principal as CustomUser).userId.toString()) != null
            ) {
                refreshAccessToken(response)
            }
            chain.doFilter(request, response)
        } catch (e: Exception) {
            logger.error("Error occurred during JWT request filter: ${e.message}")
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error")
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val cookies = request.cookies ?: return null
        return cookies.find { it.name == TokenType.toCookieName(TokenType.ACCESS) }?.value
    }

    private fun refreshAccessToken(response: HttpServletResponse) {
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            val accessTokenInfo = jwtManager.generateAccessToken(authentication)
            val accessTokenCookie = createAccessTokenCookie(accessTokenInfo.token)
            response.addCookie(accessTokenCookie)
        } catch (e: Exception) {
            logger.error("Error occurred during refreshing access token: ${e.message}")
        }
    }

    private fun createAccessTokenCookie(token: String): Cookie {
        return Cookie(TokenType.toCookieName(TokenType.ACCESS), token).apply {
            isHttpOnly = true
            path = "/"
            maxAge = (accessTokenExpMillis / 1000).toInt()
        }
    }
}
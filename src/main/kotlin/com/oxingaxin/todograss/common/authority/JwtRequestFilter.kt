package com.oxingaxin.todograss.common.authority

import com.oxingaxin.todograss.common.dto.TokenType
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtRequestFilter(
    private val jwtManager: JwtManager
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        logger.info("doFilterInternal called")
        val token = resolveToken(request)

        if (token != null && jwtManager.validateToken(token)) {
            val authentication = jwtManager.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        chain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val cookies = request.cookies ?: return null
        return cookies.find { it.name == TokenType.toCookieName(TokenType.ACCESS) }?.value
    }
}
package com.oxingaxin.todograss.common.dto

data class TokenInfo(
    val tokenType: TokenType,
    val token: String,
)

enum class TokenType {
    ACCESS, REFRESH;

    companion object {
        fun toCookieName(type: TokenType): String {
            return when (type) {
                ACCESS -> "access_token"
                REFRESH -> "refresh_token"
            }
        }
    }
}
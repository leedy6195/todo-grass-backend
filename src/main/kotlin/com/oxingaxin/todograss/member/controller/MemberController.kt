package com.oxingaxin.todograss.member.controller

import com.oxingaxin.todograss.common.authority.JwtManager
import com.oxingaxin.todograss.common.dto.BaseResponse
import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.common.dto.TokenInfo
import com.oxingaxin.todograss.common.dto.TokenType
import com.oxingaxin.todograss.member.domain.dto.MemberDtoRequest
import com.oxingaxin.todograss.member.domain.dto.SigninDto
import com.oxingaxin.todograss.member.service.MemberService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import kotlin.math.sign

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val jwtManager: JwtManager
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody memberDtoRequest: MemberDtoRequest
    ): BaseResponse<Unit> {
        memberService.signup(memberDtoRequest)

        return BaseResponse.created(null)
    }

    @PostMapping("/signin")
    fun signin(
        @RequestBody signinDto: SigninDto,
        response: HttpServletResponse
    ): BaseResponse<TokenInfo> {
        val tokenInfo = memberService.signin(signinDto)

        val cookie = Cookie(TokenType.toCookieName(TokenType.REFRESH), tokenInfo.token)
            .apply {
                isHttpOnly = true
                path = "/"
                maxAge = 60 * 10
            }

        response.addCookie(cookie)
        return BaseResponse.ok(tokenInfo)
    }

    @PostMapping("/signout")
    fun signout(
        @CookieValue(value="refresh_token", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
        memberService.signout(userId)
        val signoutCookie = Cookie(TokenType.toCookieName(TokenType.REFRESH), "").apply {
            isHttpOnly = true
            path = "/"
            maxAge = 0
        }
        response.addCookie(signoutCookie)
        return BaseResponse.ok(Unit)
    }

    @GetMapping("/check")
    fun check(
        @CookieValue(value = "access_token", required = false) accessToken: String?,
        @CookieValue(value = "refresh_token", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): BaseResponse<Boolean> {
        if (refreshToken != null && jwtManager.validateToken(refreshToken)) {

            if (accessToken == null || jwtManager.doesTokenExpireSoon(accessToken)) {
                //generate new access token
                jwtManager.generateToken(jwtManager.getAuthentication(refreshToken), TokenType.ACCESS)
                    .let {
                        val cookie = Cookie(TokenType.toCookieName(TokenType.ACCESS), it.token)
                            .apply {
                                isHttpOnly = true
                                path = "/"
                                maxAge = 60 * 3
                            }
                        response.addCookie(cookie)
                    }
            }
            return BaseResponse.ok(true)
        }
        return BaseResponse.ok(false)
    }
}
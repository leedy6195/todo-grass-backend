package com.oxingaxin.todograss.member.controller

import com.oxingaxin.todograss.common.auth.JwtManager
import com.oxingaxin.todograss.common.dto.BaseResponse
import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.common.dto.TokenType
import com.oxingaxin.todograss.member.domain.dto.MemberRequest
import com.oxingaxin.todograss.member.domain.dto.SigninRequest
import com.oxingaxin.todograss.member.domain.dto.PublicMemberInfo
import com.oxingaxin.todograss.member.service.MemberService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val jwtManager: JwtManager
) {
    @PostMapping("/signup")
    fun signup(
        @RequestBody memberRequest: MemberRequest
    ): BaseResponse<Unit> {
        memberService.signup(memberRequest)

        return BaseResponse.created(null)
    }

    @PostMapping("/signin")
    fun signin(
        @RequestBody signinRequest: SigninRequest,
        response: HttpServletResponse
    ): BaseResponse<PublicMemberInfo> {
        val tokenInfo = memberService.signin(signinRequest)

        val cookie = Cookie(TokenType.toCookieName(TokenType.REFRESH), tokenInfo.token)
            .apply {
                isHttpOnly = true
                path = "/"
                maxAge = 60 * 30
            }
        
        response.addCookie(cookie)
        val userId = jwtManager.getClaims(tokenInfo.token)["userId"].toString().toLong()
        //val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId

        println("login userId: $userId")
        val memberInfo = memberService.getMemberInfo(userId)
        return BaseResponse.ok(memberInfo)
    }

    @PostMapping("/signout")
    fun signout(
        @CookieValue(value="refresh_token", required = false) refreshToken: String?,
        response: HttpServletResponse,
        authentication: Authentication
    ): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
        println("logout userId: $userId")
        memberService.signout(userId)

        SecurityContextHolder.clearContext()

        val signoutRefreshCookie = Cookie(TokenType.toCookieName(TokenType.REFRESH), "").apply {
            isHttpOnly = true
            path = "/"
            maxAge = 0
        }

        val signoutAccessCookie = Cookie(TokenType.toCookieName(TokenType.ACCESS), "").apply {
            isHttpOnly = true
            path = "/"
            maxAge = 0
        }
        response.addCookie(signoutRefreshCookie)
        response.addCookie(signoutAccessCookie)

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
                                maxAge = 60 * 10
                            }
                        response.addCookie(cookie)
                    }
            }
            return BaseResponse.ok(true)
        }
        return BaseResponse.ok(false)
    }
}
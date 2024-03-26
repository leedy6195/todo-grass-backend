package com.oxingaxin.todograss.member.controller

import com.oxingaxin.todograss.common.auth.JwtManager
import com.oxingaxin.todograss.common.dto.BaseResponse
import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.common.dto.TokenType
import com.oxingaxin.todograss.member.domain.dto.MemberRequest
import com.oxingaxin.todograss.member.domain.dto.MemberUpdateRequest
import com.oxingaxin.todograss.member.domain.dto.SigninRequest
import com.oxingaxin.todograss.member.domain.dto.PublicMemberInfo
import com.oxingaxin.todograss.member.service.MemberService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val memberService: MemberService,
    private val jwtManager: JwtManager
) {

    @Value("\${jwt.expiration-millis.access-token}")
    var accessTokenExpMillis: Long = 0L


    @GetMapping("/nicknames/{nickname}")
    fun getMemberInfoByNickname(@PathVariable nickname: String): BaseResponse<PublicMemberInfo> {
        val memberInfo = memberService.getMemberInfoByNickname(nickname)
        return BaseResponse.fetched(memberInfo)
    }

    @GetMapping("/profile")
    fun getProfile(): BaseResponse<PublicMemberInfo> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
        val memberInfo = memberService.getMemberInfo(userId)
        return BaseResponse.fetched(memberInfo)
    }

    @PutMapping("/profile", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun updateProfile(@RequestPart profileImage: MultipartFile): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
        val memberUpdateRequest = MemberUpdateRequest(profileImage.bytes)
        memberService.updateMember(userId, memberUpdateRequest)
        return BaseResponse.updated(Unit)
    }


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
        val accessTokenCookie = createAccessTokenCookie(tokenInfo.token)
        response.addCookie(accessTokenCookie)

        val userId = jwtManager.getClaims(tokenInfo.token)["userId"].toString().toLong()
        val memberInfo = memberService.getMemberInfo(userId)

        return BaseResponse.ok(memberInfo)
    }

    @PostMapping("/signout")
    fun signout(
        response: HttpServletResponse,
        authentication: Authentication
    ): BaseResponse<Unit> {
        val userId = (SecurityContextHolder.getContext().authentication.principal as CustomUser).userId
        memberService.signout(userId)

        SecurityContextHolder.clearContext()

        val emptyCookie = createEmptyCookie()
        response.addCookie(emptyCookie)

        return BaseResponse.ok(Unit)
    }



    @GetMapping("/check")
    fun check(
        @CookieValue(value = "access_token", required = false) accessToken: String?,
        response: HttpServletResponse
    ): BaseResponse<Boolean> {

        if (accessToken != null && jwtManager.validateToken(accessToken)) {
            return BaseResponse.ok(true)
        }
        return BaseResponse.ok(false)
    }

    private fun createAccessTokenCookie(token: String): Cookie {
        return Cookie(TokenType.toCookieName(TokenType.ACCESS), token).apply {
            isHttpOnly = true
            path = "/"
            maxAge = (accessTokenExpMillis / 1000).toInt()
        }
    }

    private fun createEmptyCookie(): Cookie {
        return Cookie(TokenType.toCookieName(TokenType.ACCESS), "").apply {
            isHttpOnly = true
            path = "/"
            maxAge = 0
        }
    }
}
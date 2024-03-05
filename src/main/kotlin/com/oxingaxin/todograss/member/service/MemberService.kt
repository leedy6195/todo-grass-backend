package com.oxingaxin.todograss.member.service

import com.oxingaxin.todograss.common.authority.JwtManager
import com.oxingaxin.todograss.common.dto.TokenInfo
import com.oxingaxin.todograss.common.dto.TokenType
import com.oxingaxin.todograss.common.exception.AlreadyExistsException
import com.oxingaxin.todograss.common.exception.NotFoundException
import com.oxingaxin.todograss.member.domain.dto.MemberRequest
import com.oxingaxin.todograss.member.domain.dto.SigninRequest
import com.oxingaxin.todograss.member.domain.dto.PublicMemberInfo
import com.oxingaxin.todograss.member.domain.entity.MemberRefreshToken
import com.oxingaxin.todograss.member.domain.entity.MemberRole
import com.oxingaxin.todograss.member.domain.entity.Role
import com.oxingaxin.todograss.member.repository.MemberRefreshTokenRepository
import com.oxingaxin.todograss.member.repository.MemberRepository
import com.oxingaxin.todograss.member.repository.MemberRoleRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val memberRefreshTokenRepository: MemberRefreshTokenRepository,
    private val authenticationManagerBuilder: AuthenticationManagerBuilder,
    private val jwtManager: JwtManager
) {

    fun signup(memberRequest: MemberRequest) {
        if (memberRepository.existsByEmail(memberRequest.email))
            throw AlreadyExistsException("member", "email", memberRequest.email)

        if (memberRepository.existsByNickname(memberRequest.nickname))
            throw AlreadyExistsException("member", "nickname", memberRequest.nickname)

        val member = memberRequest.toEntity()
        memberRepository.save(member)

        val memberRole = MemberRole(null, Role.MEMBER, member)
        memberRoleRepository.save(memberRole)

    }

    fun signin(signinRequest: SigninRequest): TokenInfo {
        val authenticationToken = UsernamePasswordAuthenticationToken(signinRequest.email, signinRequest.password)
        val authentication = authenticationManagerBuilder.`object`.authenticate(authenticationToken)

        val member = memberRepository.findByEmail(signinRequest.email)!!
        val refreshToken = memberRefreshTokenRepository.findByMemberId(member.id!!)

        val newRefreshToken = jwtManager.generateToken(authentication, TokenType.REFRESH)

        if (refreshToken == null) {
            memberRefreshTokenRepository.save(MemberRefreshToken(member = member, refreshToken = newRefreshToken.token))
        } else {
            refreshToken.updateRefreshToken(newRefreshToken.token)
        }

        return newRefreshToken
    }

    fun getMemberInfo(memberId: Long): PublicMemberInfo {
        return memberRepository.findById(memberId).orElseThrow { NotFoundException("member") }.let {
            PublicMemberInfo(it.email, it.nickname!!)
        }
    }

    fun signout(memberId: Long) {
        val refreshToken = memberRefreshTokenRepository.findByMemberId(memberId)
            ?: throw NotFoundException("refreshToken", "memberId", memberId.toString())

        memberRefreshTokenRepository.delete(refreshToken)
    }

}
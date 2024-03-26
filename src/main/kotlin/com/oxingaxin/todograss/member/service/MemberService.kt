package com.oxingaxin.todograss.member.service

import com.oxingaxin.todograss.common.auth.JwtManager
import com.oxingaxin.todograss.common.aws.S3Service
import com.oxingaxin.todograss.common.dto.TokenInfo
import com.oxingaxin.todograss.common.exception.AlreadyExistsException
import com.oxingaxin.todograss.common.exception.NotFoundException
import com.oxingaxin.todograss.common.redis.RedisDao
import com.oxingaxin.todograss.member.domain.dto.MemberRequest
import com.oxingaxin.todograss.member.domain.dto.MemberUpdateRequest
import com.oxingaxin.todograss.member.domain.dto.SigninRequest
import com.oxingaxin.todograss.member.domain.dto.PublicMemberInfo
import com.oxingaxin.todograss.member.domain.entity.MemberRole
import com.oxingaxin.todograss.member.domain.entity.Role
import com.oxingaxin.todograss.member.repository.MemberRepository
import com.oxingaxin.todograss.member.repository.MemberRoleRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Transactional
@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberRoleRepository: MemberRoleRepository,
    private val authenticationManagerBuilder: AuthenticationManagerBuilder,
    private val jwtManager: JwtManager,
    private val redisDao: RedisDao,
    private val s3Service: S3Service
) {

    @Value("\${jwt.expiration-millis.refresh-token}")
    var refreshTokenExpMillis: Long = 0L


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

        val accessToken = jwtManager.generateAccessToken(authentication)
        val refreshToken = jwtManager.generateRefreshToken(authentication)

        val member = memberRepository.findByEmail(signinRequest.email)!!
        redisDao.setValue(member.id.toString(), refreshToken.token, Duration.ofMillis(refreshTokenExpMillis))

        return accessToken
    }

    fun getMemberInfo(memberId: Long): PublicMemberInfo {
        return memberRepository.findById(memberId).orElseThrow { NotFoundException("member") }.let {
            PublicMemberInfo(it.email, it.nickname!!, it.profileImgPath!!)
        }
    }

    fun getMemberInfoByNickname(nickname: String) : PublicMemberInfo {
        return memberRepository.findByNickname(nickname).orElseThrow { NotFoundException("member") }.let {
            PublicMemberInfo(it.email, it.nickname!!, it.profileImgPath!!)
        }
    }

    fun signout(memberId: Long) {
        if (redisDao.getValue(memberId.toString()) == null) { throw NotFoundException("refreshToken") }
        redisDao.deleteValue(memberId.toString())
    }

    fun updateMember(memberId: Long, memberUpdateRequest: MemberUpdateRequest) {
        val member = memberRepository.findById(memberId).orElseThrow{ NotFoundException("member") }
        val nickname = member.nickname!!
        s3Service.uploadFile(memberUpdateRequest.profileImage, nickname)
        member.profileImgPath = "/images/$nickname"
    }

}
package com.oxingaxin.todograss.common.service

import com.oxingaxin.todograss.common.dto.CustomUser
import com.oxingaxin.todograss.member.domain.entity.Member
import com.oxingaxin.todograss.member.repository.MemberRepository
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    ) : UserDetailsService {

    private val logger = LoggerFactory.getLogger(javaClass)


    override fun loadUserByUsername(username: String): UserDetails {
        logger.info("loadUserByUsername called")

        return memberRepository.findByEmail(username)
            ?.let { createUserDetails(it) } ?: throw UsernameNotFoundException("해당 유저는 없습니다.")
    }


    private fun createUserDetails(member: Member): UserDetails =
        CustomUser(
            member.id!!,
            member.email,
            passwordEncoder.encode(member.password),
            member.memberRole!!.map { SimpleGrantedAuthority("ROLE_${it.role}") }
        )
}
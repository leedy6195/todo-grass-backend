package com.oxingaxin.todograss.member.domain.entity

import jakarta.persistence.*


@Entity
class MemberRefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    val member: Member,

    private var refreshToken: String,

    private var reissueCount : Int = 0
) {
    fun updateRefreshToken(refreshToken: String) {
        this.refreshToken = refreshToken
    }

    fun increaseReissueCount() {
        this.reissueCount++
    }
}
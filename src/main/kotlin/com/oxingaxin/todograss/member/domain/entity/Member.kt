package com.oxingaxin.todograss.member.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, length = 50)
    val email: String,

    @Column(length = 50)
    val password: String,

    @Column(unique = true, length = 20)
    val nickname: String?,

    var profileImgPath: String? = null,

    private var createdAt: LocalDateTime? = null,
) {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    val memberRole: List<MemberRole>? = null

    @PrePersist
    fun prePersist() {
        createdAt = LocalDateTime.now()
        profileImgPath = "https://avatars.githubusercontent.com/u/583231?s=400&v=4"
    }
}
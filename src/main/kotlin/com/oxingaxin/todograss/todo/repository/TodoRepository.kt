package com.oxingaxin.todograss.todo.repository

import com.oxingaxin.todograss.todo.domain.entity.TodoItem
import org.springframework.data.jpa.repository.JpaRepository

interface TodoRepository : JpaRepository<TodoItem, Long> {
    fun findAllByMemberIdOrderByIdDesc(memberId: Long): List<TodoItem>
}
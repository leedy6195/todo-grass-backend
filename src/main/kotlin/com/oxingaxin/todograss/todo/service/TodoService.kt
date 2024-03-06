package com.oxingaxin.todograss.todo.service

import com.oxingaxin.todograss.common.exception.NotFoundException
import com.oxingaxin.todograss.member.repository.MemberRepository
import com.oxingaxin.todograss.todo.domain.dto.TodoItemRequest
import com.oxingaxin.todograss.todo.domain.dto.TodoItemResponse
import com.oxingaxin.todograss.todo.domain.entity.TodoItem
import com.oxingaxin.todograss.todo.repository.TodoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class TodoService(
    private val todoRepository: TodoRepository,
    private val memberRepository: MemberRepository
) {

    fun addTodo(todoItemRequest: TodoItemRequest) {
        val member = memberRepository.findById(todoItemRequest.memberId!!)
            .orElseThrow { NotFoundException("member") }
        val todoItem = TodoItem.Builder()
            .member(member)
            .title(todoItemRequest.title)
            .description(todoItemRequest.description)
            .build()

        todoRepository.save(todoItem)
    }

    fun updateTodo(id: Long, todoItemRequest: TodoItemRequest) {
        val todoItem = todoRepository.findById(id)
            .orElseThrow { NotFoundException("todo") }

        todoItem.apply {
            title = todoItemRequest.title
            description = todoItemRequest.description
        }
    }

    fun getTodo(id: Long): TodoItemResponse {
        val todo = todoRepository.findById(id)
            .orElseThrow { NotFoundException("todo") }

        return TodoItemResponse(todo.id!!, todo.title, todo.description)
    }

    fun getTodos(nickname: String): List<TodoItemResponse> {
        val member = memberRepository.findByNickname(nickname)
            .orElseThrow { NotFoundException("member", "nickname", nickname) }

        val todos = todoRepository.findAllByMemberIdOrderByIdDesc(member.id!!)
        return todos.map { todo ->
            TodoItemResponse(todo.id!!, todo.title, todo.description)
        }
    }
    fun deleteTodo(id: Long) {
        todoRepository.deleteById(id)
    }
}
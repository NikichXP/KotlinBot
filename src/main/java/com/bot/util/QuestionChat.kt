package com.bot.util

import java.util.LinkedList
import com.bot.entity.State

class QuestionChat() {
	
	val actions = LinkedList<Pair<String, (String) -> Unit>>()
	lateinit var afterAll: () -> Unit
	var isCompleted = false
		get() = actions.peek() == null
	var endState: State = State.HELLO
	
	constructor(text: String, action: (String) -> Unit) : this() {
		then(text, action)
	}
	
	fun then(text: String, action: (String) -> Unit): QuestionChat {
		actions.add(Pair(text, action))
		return this
	}
	
	fun afterAll(action: () -> Unit): QuestionChat {
		this.afterAll = action
		return this
	}
	
	fun next() = actions.poll()
	fun endState(endState: State): QuestionChat {
		this.endState = endState
		return this
	}
	
}
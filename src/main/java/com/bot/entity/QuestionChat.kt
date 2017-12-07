package com.bot.entity

import java.util.LinkedList

class QuestionChat() {
	
	val actions = LinkedList<Pair<String, (String) -> Unit>>()
	var isCompleted = false
		get() = actions.peek() == null
	
	var isNextIsChat = false
	var nextState: State = State.HELLO
	
	var onEndAction: () -> Unit = {}
	
	/** this is used only if isNextIsChat is true */
	private lateinit var nextChatDeterminer: (String) -> QuestionChat
	
	constructor(text: String, action: (String) -> Unit) : this() {
		then(text, action)
	}
	
	fun then(text: String, action: (String) -> Unit): QuestionChat {
		actions.add(Pair(text, action))
		return this
	}
	
	fun setNextChatFunction(function: (String) -> QuestionChat): QuestionChat {
		nextChatDeterminer = function
		isNextIsChat = true
		return this
	}
	
	
	fun inTheEnd(state: State, action: () -> Unit = {}): QuestionChat {
		onEndAction = action
		nextState = state
		isNextIsChat = false
		return this
	}
	
	
	//	fun afterAll(action: () -> Unit): QuestionChat {
	//		this.afterAll = action
	//		return this
	//	}
	//
	//	fun next() = actions.poll()
	//
	//	fun endState(endState: State): QuestionChat {
	//		this.endState = endState
	//		return this
	//	}
	//
	//	fun jumpTo (question: String, nextFunction: (String) -> QuestionChat) {
	//
	//	}
	
}

/*

{

}

 */
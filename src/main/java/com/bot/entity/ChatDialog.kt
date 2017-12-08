package com.bot.entity

import java.util.LinkedList

// TODO Доделать error-хэндлинг
class ChatDialog(val userid: String? = null) {
	
	val actions = LinkedList<Pair<Response, (String) -> Unit>>()
	/** errorDeterminer - errorMessage - nextDialog */
	val errorHandler = LinkedList<Triple<() -> Boolean, String, () -> ChatDialog>>()
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: ((String) -> ChatDialog)? = null
	var onCompleteAction: (() -> Unit)? = null
	
	fun then(text: String, action: (String) -> Unit): ChatDialog {
		actions.add(Pair(Response(userid, text), action))
		return this
	}
	
	fun then(response: Response, action: (String) -> Unit): ChatDialog {
		actions.add(Pair(response, action))
		return this
	}
	
	fun setNextChatFunction(text: String, function: (String) -> ChatDialog): ChatDialog {
		nextChatQuestion = Response(userid, text)
		return setNextChatFunction(function)
	}
	
	fun setNextChatFunction(response: Response, function: (String) -> ChatDialog): ChatDialog {
		nextChatQuestion = response
		return setNextChatFunction(function)
	}
	
	fun setNextChatFunction(function: (String) -> ChatDialog): ChatDialog {
		nextChatDeterminer = function
		return this
	}
	
	fun setEachStepFunction(function: () -> Unit): ChatDialog {
		this.eachStepAction = function
		return this
	}
	
	fun setOnCompleteAction(action: (() -> Unit)): ChatDialog {
		this.onCompleteAction = action
		return this
	}
}
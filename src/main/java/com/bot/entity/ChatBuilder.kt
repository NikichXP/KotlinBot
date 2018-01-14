package com.bot.entity

import java.util.LinkedList

class ChatBuilder(val userid: String? = null) {
	
	val actions = LinkedList<Pair<Response, (String) -> Unit>>()
	var beforeExecution: (() -> Unit)? = null
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: ((String) -> ChatBuilder)? = null
	var onCompleteAction: (() -> Unit)? = null
	var onCompleteMessage: Response? = null
	var name: String = "_noname"
	
	val errorHandler: Pair<(Exception) -> Boolean, (String) -> ChatBuilder> = Pair({ e -> true },
		{ string -> this.nextChatDeterminer!!.invoke(string) })
	
	fun then(text: String, action: (String) -> Unit //,
		//	         errorHandler: (Exception) -> Boolean = { false }, errorMessage: String = "Unexpected error",
		//	         postFixHandler: () -> ChatDialog =
		//	         { throw IllegalStateException("Unexpected error") }
	): ChatBuilder {
		actions.add(Pair(Response(userid, text), action))
		//		errorHandler.add(Triple(isErrorFunction, errorMessage, postFixHandler))
		return this
	}
	
	fun then(response: Response, action: (String) -> Unit //,
		//	         isErrorFunction: () -> Boolean = { false }, errorMessage: String = "Unexpected error",
		//	         postFixHandler: () -> ChatDialog =
		//	         { throw IllegalStateException("Unexpected error") }
	): ChatBuilder {
		actions.add(Pair(response, action))
		//		errorHandler.add(Triple(isErrorFunction, errorMessage, postFixHandler))
		return this
	}
	
	fun beforeExecution(action: () -> Unit): ChatBuilder {
		this.beforeExecution = action
		return this
	}
	
	fun setNextChatFunction(text: String, function: (String) -> ChatBuilder): ChatBuilder {
		nextChatQuestion = Response(userid, text)
		return setNextChatFunction(function)
	}
	
	fun setNextChatFunction(response: Response, function: (String) -> ChatBuilder): ChatBuilder {
		nextChatQuestion = response
		return setNextChatFunction(function)
	}
	
	fun setNextChatFunction(responseFx: () -> String, function: (String) -> ChatBuilder): ChatBuilder {
		nextChatQuestion = Response(responseFx)
		return setNextChatFunction(function)
	}
	
	fun setNextChatFunction(function: (String) -> ChatBuilder): ChatBuilder {
		nextChatDeterminer = function
		return this
	}
	
	fun setEachStepFunction(function: () -> Unit): ChatBuilder {
		this.eachStepAction = function
		return this
	}
	
	fun setOnCompleteAction(action: (() -> Unit)): ChatBuilder {
		this.onCompleteAction = action
		return this
	}
	
	fun setOnCompleteMessage(message: Response): ChatBuilder {
		this.onCompleteMessage = message
		return this
	}
	
	fun name(name: String): ChatBuilder {
		this.name = name
		return this
	}
}
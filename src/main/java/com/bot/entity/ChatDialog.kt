package com.bot.entity

import java.util.LinkedList

class ChatDialog(val userid: String? = null) {
	
	val actions = LinkedList<Pair<Response, (String) -> Unit>>()
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: ((String) -> ChatDialog)? = null
	var onCompleteAction: (() -> Unit)? = null
	
	val errorHandler: Pair<(Exception) -> Boolean, (String) -> ChatDialog> = Pair({ e -> true },
		{ string -> this.nextChatDeterminer!!.invoke(string) })
	
	fun then(text: String, action: (String) -> Unit //,
		//	         errorHandler: (Exception) -> Boolean = { false }, errorMessage: String = "Unexpected error",
		//	         postFixHandler: () -> ChatDialog =
		//	         { throw IllegalStateException("Unexpected error") }
	): ChatDialog {
		actions.add(Pair(Response(userid, text), action))
		//		errorHandler.add(Triple(isErrorFunction, errorMessage, postFixHandler))
		return this
	}
	
	fun then(response: Response, action: (String) -> Unit //,
		//	         isErrorFunction: () -> Boolean = { false }, errorMessage: String = "Unexpected error",
		//	         postFixHandler: () -> ChatDialog =
		//	         { throw IllegalStateException("Unexpected error") }
	): ChatDialog {
		actions.add(Pair(response, action))
		//		errorHandler.add(Triple(isErrorFunction, errorMessage, postFixHandler))
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
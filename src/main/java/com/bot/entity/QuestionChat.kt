package com.bot.entity

import java.util.LinkedList

class QuestionChat(val userid: String? = null) {
	
	val actions = LinkedList<Pair<Response, (String) -> Unit>>()
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: ((String) -> QuestionChat)? = null
	var onCompleteAction: (() -> Unit)? = null
	
	fun then(text: String, action: (String) -> Unit): QuestionChat {
		actions.add(Pair(Response(userid, text), action))
		return this
	}
	
	fun then(response: Response, action: (String) -> Unit): QuestionChat {
		actions.add(Pair(response, action))
		return this
	}
	
	fun setNextChatFunction(text: String, function: (String) -> QuestionChat): QuestionChat {
		nextChatQuestion = Response(userid, text)
		return setNextChatFunction(function)
	}
	
	fun setNextChatFunction(response: Response, function: (String) -> QuestionChat): QuestionChat {
		nextChatQuestion = response
		return setNextChatFunction(function)
	}
	
	fun setNextChatFunction(function: (String) -> QuestionChat): QuestionChat {
		nextChatDeterminer = function
		return this
	}
	
	fun setEachStepFunction(function: () -> Unit): QuestionChat {
		this.eachStepAction = function
		return this
	}
	
	fun setOnCompleteAction(action: (() -> Unit)): QuestionChat {
		this.onCompleteAction = action
		return this
	}
}
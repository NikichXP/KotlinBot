package com.bot.entity

import com.bot.chats.BaseChats
import java.util.LinkedList

class ChatBuilder(val user: User? = null) {
	
	val actions = LinkedList<Pair<Response, (String) -> Unit>>()
	var beforeExecution: (() -> Unit)? = null
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: (String) -> ChatBuilder = { BaseChats.hello(user!!) }
	var onCompleteAction: (() -> Unit)? = null
	var onCompleteMessage: Response? = null
	var afterWorkAction: (() -> Unit)? = null
	var name: String = "_noname"
	
	val errorHandler: Pair<(Exception) -> Boolean, (String) -> ChatBuilder> = Pair({ e -> true },
		{ string -> this.nextChatDeterminer.invoke(string) })
	
	fun then(text: String, action: (String) -> Unit): ChatBuilder {
		actions.add(Pair(Response(user?.id, text), action))
		return this
	}
	
	fun then(response: Response, action: (String) -> Unit): ChatBuilder {
		actions.add(Pair(response, action))
		return this
	}
	
	fun beforeExecution(action: () -> Unit): ChatBuilder {
		this.beforeExecution = action
		return this
	}
	
	fun setNextChatFunction(text: String, function: (String) -> ChatBuilder): ChatBuilder {
		nextChatQuestion = Response(user?.id, text)
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
	
	fun setOnCompleteMessage(message: String): ChatBuilder {
		this.onCompleteMessage = Response(message)
		return this
	}
	
	fun setAfterWorkAction(action: () -> Unit): ChatBuilder {
		this.afterWorkAction = action
		return this
	}
	
	fun name(name: String): ChatBuilder {
		this.name = name
		return this
	}
}
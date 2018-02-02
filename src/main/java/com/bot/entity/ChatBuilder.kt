package com.bot.entity

import com.bot.chats.BaseChats
import java.util.LinkedList

class ChatBuilder(val user: User) {
	
	val actions = LinkedList<Pair<Response, (String) -> Unit>>()
	var beforeExecution: (() -> Unit)? = null
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: (String) -> ChatBuilder = { BaseChats.hello(user) }
	var onCompleteAction: (() -> Unit)? = null
	var onCompleteMessage: Response? = null
	var afterWorkAction: (() -> Unit)? = null
	var name: String = "_noname"
	
	val errorHandler: Pair<(Exception) -> Boolean, (String) -> ChatBuilder> = Pair({ e -> true },
		{ string -> this.nextChatDeterminer.invoke(string) })
	
	fun then(text: String, action: (String) -> Unit) = also { actions.add(Pair(Response(user.id, text), action)) }
	fun then(response: Response, action: (String) -> Unit) = also { actions.add(Pair(response, action)) }
	
	fun beforeExecution(action: () -> Unit) = also { this.beforeExecution = action }
	
	fun setNextChatFunction(text: String, function: (String) -> ChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = Response(user.id, text) }
	fun setNextChatFunction(response: Response, function: (String) -> ChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = response }
	fun setNextChatFunction(responseFx: () -> String, function: (String) -> ChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = Response(responseFx) }
	fun setNextChatFunction(function: (String) -> ChatBuilder) = also { nextChatDeterminer = function }
	
	fun setEachStepFunction(function: () -> Unit) = also { this.eachStepAction = function }
	fun setAfterWorkAction(action: () -> Unit) = also { this.afterWorkAction = action }
	
	fun setOnCompleteAction(action: (() -> Unit)) = also { this.onCompleteAction = action }
	fun setOnCompleteMessage(message: Response) = also { this.onCompleteMessage = message }
	fun setOnCompleteMessage(message: String) = also { this.onCompleteMessage = Response(user, message) }
	
	fun name(name: String) = also { this.name = name }
}
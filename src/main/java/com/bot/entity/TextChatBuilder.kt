package com.bot.entity

import com.bot.chats.BaseChats
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import java.util.LinkedList

class TextChatBuilder(val user: User) : AbstractChatBuilder {
	
	val actions = LinkedList<ChatAction>()
	var beforeExecution: (() -> Unit)? = null
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: (String) -> AbstractChatBuilder = { BaseChats.hello(user) }
	var onCompleteAction: (() -> Unit)? = null
	var onCompleteMessage: Response? = null
	var afterWorkAction: (() -> Unit)? = null
	var name: String = "_noname"
	
	override fun toMessageDrivenChat(): MessageChatBuilder {
		val ret = MessageChatBuilder(user)
		ret.actions.clear()
		this.actions.forEach { ret.actions.add(it.toMessageAction()) }
		ret.beforeExecution = this.beforeExecution
		ret.eachStepAction = this.eachStepAction
		ret.nextChatQuestion = this.nextChatQuestion
		ret.nextChatDeterminer = { this.nextChatDeterminer(it.text!!) }
		ret.onCompleteAction = this.onCompleteAction
		ret.onCompleteMessage = this.onCompleteMessage
		ret.afterWorkAction = this.afterWorkAction
		ret.name = this.name
		return ret
	}
	
	val errorHandler: Pair<(Exception) -> Boolean, (String) -> AbstractChatBuilder> = Pair({ e -> true },
		{ string -> this.nextChatDeterminer.invoke(string) })
	
	fun then(text: String, action: (String) -> Unit) = also { actions.add(DefaultChatAction(Response(user.id, text), action)) }
	fun then(response: Response, action: (String) -> Unit) = also { actions.add(DefaultChatAction(response.ensureUser(user.id), action)) }
	fun thenIf(optional: () -> Boolean, text: String, action: (String) -> Unit) = also {
		actions.add(
			OptionalChatAction(optional, Response(user.id, text), action)
		)
	}
	
	fun thenIf(optional: () -> Boolean, response: Response, action: (String) -> Unit) = also {
		actions.add(
			OptionalChatAction(optional, response, action)
		)
	}
	
	fun beforeExecution(action: () -> Unit) = also { this.beforeExecution = action }
	
	fun setNextChatFunction(text: String, function: (String) -> TextChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = Response(user.id, text) }
	fun setNextChatFunction(response: Response, function: (String) -> TextChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = response.ensureUser(user.id) }
	fun setNextChatFunction(responseFx: () -> String, function: (String) -> TextChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = Response(responseFx) }
	fun setNextChatFunction(function: (String) -> TextChatBuilder) = also { nextChatDeterminer = function }
	
	fun setEachStepFunction(function: () -> Unit) = also { this.eachStepAction = function }
	fun setAfterWorkAction(action: () -> Unit) = also { this.afterWorkAction = action }
	
	fun setOnCompleteAction(action: (() -> Unit)) = also { this.onCompleteAction = action }
	fun setOnCompleteMessage(message: Response) = also { this.onCompleteMessage = message.ensureUser(user.id) }
	fun setOnCompleteMessage(message: String) = also { this.onCompleteMessage = Response(user, message) }
	
	fun name(name: String) = also { this.name = name }
	
	
}

interface AbstractChatBuilder {
	fun toMessageDrivenChat(): MessageChatBuilder
}

class MessageChatBuilder(val user: User) : AbstractChatBuilder {
	
	val actions = LinkedList<MessageChatAction>()
	var beforeExecution: (() -> Unit)? = null
	var eachStepAction: (() -> Unit)? = null
	var nextChatQuestion: Response? = null
	var nextChatDeterminer: (Message) -> AbstractChatBuilder = { BaseChats.hello(user).toMessageDrivenChat() }
	var onCompleteAction: (() -> Unit)? = null
	var onCompleteMessage: Response? = null
	var afterWorkAction: (() -> Unit)? = null
	var name: String = "_noname"
	
	override fun toMessageDrivenChat() = this
	
	val errorHandler: Pair<(Exception) -> Boolean, (Message) -> AbstractChatBuilder> = Pair({ e -> true },
		{ string -> this.nextChatDeterminer.invoke(string) })
	
	fun then(text: String, action: (Message) -> Unit) = also { actions.add(DefaultMessageChatAction(Response(user.id, text), action)) }
	fun then(response: Response, action: (Message) -> Unit) = also { actions.add(DefaultMessageChatAction(response.ensureUser(user.id), action)) }
	fun thenIf(optional: () -> Boolean, text: String, action: (Message) -> Unit) = also {
		actions.add(
			OptionalMessageChatAction(optional, Response(user.id, text), action)
		)
	}
	
	fun thenIf(optional: () -> Boolean, response: Response, action: (Message) -> Unit) = also {
		actions.add(
			OptionalMessageChatAction(optional, response, action)
		)
	}
	
	fun cycleAction(cycleCondition: (Message) -> Boolean, response: Response, action: (Message) -> Unit, postAction: () -> Unit = {}) = also {
		actions.add(CycledMessageChatAction(cycleCondition, response.ensureUser(user.id), action).also { it.postCycleAction = postAction })
	}
	
	fun beforeExecution(action: () -> Unit) = also { this.beforeExecution = action }
	
	fun setNextChatFunction(text: String, function: (Message) -> MessageChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = Response(user.id, text) }
	fun setNextChatFunction(response: Response, function: (Message) -> MessageChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = response.ensureUser(user.id) }
	fun setNextChatFunction(responseFx: () -> String, function: (Message) -> MessageChatBuilder) = setNextChatFunction(function).also { nextChatQuestion = Response(responseFx) }
	fun setNextChatFunction(function: (Message) -> MessageChatBuilder) = also { nextChatDeterminer = function }
	
	fun setEachStepFunction(function: () -> Unit) = also { this.eachStepAction = function }
	fun setAfterWorkAction(action: () -> Unit) = also { this.afterWorkAction = action }
	
	fun setOnCompleteAction(action: (() -> Unit)) = also { this.onCompleteAction = action }
	fun setOnCompleteMessage(message: Response) = also { this.onCompleteMessage = message.ensureUser(user.id) }
	fun setOnCompleteMessage(message: String) = also { this.onCompleteMessage = Response(user, message) }
	
	fun name(name: String) = also { this.name = name }
	
}

interface AbstractChatAction<T> {
	
	suspend fun handle(lock: Mutex)
	suspend fun action(message: T)
	fun isCompleted(message: T): Boolean
	
}

interface ChatAction : AbstractChatAction<String> {
	
	override suspend fun handle(lock: Mutex)
	override suspend fun action(message: String)
	override fun isCompleted(message: String): Boolean
	fun toMessageAction(): MessageChatAction
	
}

interface MessageChatAction : AbstractChatAction<Message> {
	override suspend fun handle(lock: Mutex)
	override suspend fun action(message: Message)
	override fun isCompleted(message: Message): Boolean
}

class DefaultChatAction(var response: Response, var action: (String) -> Unit) : ChatAction {
	
	private var isCompleted = false
	
	override suspend fun handle(lock: Mutex) {
		response.send()
		lock.lock()
	}
	
	override suspend fun action(message: String) {
		this.action.invoke(message)
		isCompleted = true
	}
	
	override fun isCompleted(message: String) = isCompleted
	
	override fun toMessageAction(): MessageChatAction {
		return DefaultMessageChatAction(response, { launch { action(it.text!!) } })
	}
	
}

class DefaultMessageChatAction(var response: Response, var action: (Message) -> Unit) : MessageChatAction {
	
	private var isCompleted = false
	
	override suspend fun handle(lock: Mutex) {
		response.send()
		lock.lock()
	}
	
	override suspend fun action(message: Message) {
		this.action.invoke(message)
		isCompleted = true
	}
	
	override fun isCompleted(message: Message) = isCompleted
	
}

class OptionalChatAction(var workOrNot: () -> Boolean, var response: Response, var action: (String) -> Unit) : ChatAction {
	
	private var isCompleted: Boolean
	private var isRequestSent: Boolean = false
	
	init {
		isCompleted = !(workOrNot.invoke())
	}
	
	override suspend fun handle(lock: Mutex) {
		
		if (workOrNot.invoke()) {
			response.send()
			isRequestSent = true
			isCompleted = false
			lock.lock()
		}
	}
	
	override suspend fun action(message: String) {
		if (isRequestSent) {
			this.action.invoke(message)
		}
		isCompleted = true
	}
	
	override fun toMessageAction(): MessageChatAction = OptionalMessageChatAction(workOrNot, response, { launch { action(it.text!!) } })
	
	override fun isCompleted(message: String) = isCompleted
	
}

class OptionalMessageChatAction(var workOrNot: () -> Boolean, var response: Response, var action: (Message) -> Unit) : MessageChatAction {
	
	private var isCompleted: Boolean
	private var isRequestSent: Boolean = false
	
	init {
		isCompleted = !(workOrNot.invoke())
	}
	
	override suspend fun handle(lock: Mutex) {
		
		if (workOrNot.invoke()) {
			response.send()
			isRequestSent = true
			isCompleted = false
			lock.lock()
		}
	}
	
	override suspend fun action(message: Message) {
		if (isRequestSent) {
			this.action.invoke(message)
		}
		isCompleted = true
	}
	
	override fun isCompleted(message: Message) = isCompleted
	
}

class CycledMessageChatAction(var cycleCondition: (Message) -> Boolean, var response: Response, var action: (Message) -> Unit) : MessageChatAction {
	
	private lateinit var lock: Mutex
	var postCycleAction: () -> Unit = {}
	
	override suspend fun handle(lock: Mutex) {
		response.send()
		this.lock = lock.also { it.lock() }
	}
	
	override suspend fun action(message: Message) {
		action.invoke(message)
		response.send()
		lock.lock()
	}
	
	override fun isCompleted(message: Message): Boolean {
		if (cycleCondition(message)) {
			return false
		}
		postCycleAction()
		return true
	}
	
}
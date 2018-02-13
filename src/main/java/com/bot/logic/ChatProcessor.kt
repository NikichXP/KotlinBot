package com.bot.logic

import com.bot.chats.BaseChats
import com.bot.chats.RegisterChat
import com.bot.entity.*
import com.bot.tgapi.Method
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import java.time.LocalDateTime
import java.util.concurrent.Semaphore

open class ChatProcessor(val user: User) {
	
	var chat: ChatBuilder =
		if (user.type == User.Companion.Type.NONAME) RegisterChat(user).getChat()
		else BaseChats.hello(user)
	var message: String = "/home"
	private val lock = Mutex()
	
	init {
		launch {
			while (true) {
				work()
			}
		}
	}
	
	/**
	 * Procession order:
	 * 1. Send message, wait for response. Invoke the response.
	 * 2. Check if error happens. if error == true -> invoke error function and nextChatDeterminer function
	 * 3. invoke onEachStepAction
	 * After all:
	 * 4. onCompleteAction + nextChatQuestion
	 * 5. nextChatDeterminer
	 */
	private suspend fun work() {
		chat.beforeExecution?.invoke()
		try {
			var selectedAction: ChatAction
			var i = 0
			while (i < chat.actions.size) {
				if (i < 0) {
					i = 0
				}
				selectedAction = chat.actions[i++]
				selectedAction.handle(lock)
				while (!selectedAction.isCompleted()) {
					if (message.contains("\uD83C\uDFE0") || message == "/help" || message == "/home") {
						chat = BaseChats.hello(user)
						return
					}
					when (message) {
						"/back" -> {
							i -= 2
						}
						else    -> {
							try {
								selectedAction.action(message)
								chat.eachStepAction?.invoke()
							} catch (e: Exception) {
								println("here!")
								if (chat.errorHandler.first(e)) {
									throw e
								}
							}
						}
					}
				}
			}
			
			chat.onCompleteAction?.invoke()
			chat.onCompleteMessage?.send()
		} catch (e: Exception) {
			val sb = StringBuilder().append(e.javaClass.name).append("  ->  ")
				.append(e.localizedMessage).append("\n")
			e.stackTrace
				.filter { it.className.startsWith("com.bot") }
				.forEach {
					sb.append(it.className).append(" : ").append(it.methodName).append(" @ line ")
						.append(it.lineNumber).append("\n")
				}
			sendMessage("Exception on message invoking, please send screenshot to support. Exception message:" +
				e.localizedMessage + "\nTime: ${LocalDateTime.now()}")
			e.printStackTrace()
		}
		
		chat.nextChatQuestion?.also {
			sendMessage(it)
			lock.lock()
			if (message == "/home") {
				chat = BaseChats.hello(user)
				return
			}
		}
		try {
			chat = chat.nextChatDeterminer.invoke(message)
			chat.afterWorkAction?.invoke()
		} catch (e: Exception) {
			sendMessage("Exception on nextChatDeterminer, please send screenshot to support. Exception message:" +
				e.localizedMessage + "\nTime: ${LocalDateTime.now()}")
			chat = BaseChats.hello(user)
		}
	}
	
	fun input(text: String) {
		message = text
		if (lock.isLocked)
			lock.unlock()
	}
	
	private fun sendMessage(response: Response) {
		Method.sendMessage(response.ensureUser(user.id))
	}
	
	private fun sendMessage(text: String) {
		Method.sendMessage(user.id, text)
	}
	
}
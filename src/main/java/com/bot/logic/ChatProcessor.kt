package com.bot.logic

import com.bot.chats.BaseChats
import com.bot.chats.RegisterChat
import com.bot.entity.*
import com.bot.tgapi.Method
import com.google.gson.JsonObject
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import java.time.LocalDateTime
import java.util.logging.Logger

open class ChatProcessor(val user: User) {
	
	var chat: MessageChatBuilder =
		if (user.type == User.Companion.Type.NONAME) RegisterChat(user).getChat().toMessageDrivenChat()
		else BaseChats.hello(user).toMessageDrivenChat()
	var message: Message = Message(id = "", senderId = user.id, text = "/home", textMessageJson = JsonObject())
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
			var selectedAction: MessageChatAction
			var i = 0
			while (i < chat.actions.size) {
				if (i < 0) {
					i = 0
				}
				selectedAction = chat.actions[i++]
				selectedAction.handle(lock)
				while (!selectedAction.isCompleted(message)) {
					if (message.text?.contains("\uD83C\uDFE0") == true ||
						message.text?.equals("/help") == true ||
						message.text?.equals("/home") == true) {
						chat = if (message.text?.equals("/help") == true) {
							BaseChats.getHelp(user).toMessageDrivenChat()
						} else {
							BaseChats.hello(user).toMessageDrivenChat()
						}
						return
					}
					when (message.text) {
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
			if (message.text?.equals("/home") == true) {
				chat = BaseChats.hello(user).toMessageDrivenChat()
				return
			}
			if (message.text?.equals("/help") == true) {
				chat = BaseChats.getHelp(user).toMessageDrivenChat()
				return
			}
		}
		try {
			chat = chat.nextChatDeterminer.invoke(message).toMessageDrivenChat()
			chat.afterWorkAction?.invoke()
		} catch (e: Exception) {
			sendMessage("Exception on nextChatDeterminer, please send screenshot to support. Exception message:" +
				e.localizedMessage + "\nTime: ${LocalDateTime.now()}")
			e.printStackTrace()
			chat = BaseChats.hello(user).toMessageDrivenChat()
		}
	}
	
	fun input(text: Message) {
		message = text
		if (lock.isLocked)
			lock.unlock()
	}
	
	private fun sendMessage(response: Response) {
		try {
			Method.sendMessage(response.ensureUser(user.id))
		} catch (e: Exception) {
			Logger.getAnonymousLogger().info("Here is exception, don't worry.")
		}
	}
	
	private fun sendMessage(text: String) {
		Method.sendMessage(user.id, text)
	}
	
}
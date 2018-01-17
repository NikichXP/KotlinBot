package com.bot.logic

import com.bot.chats.BaseChats
import com.bot.chats.RegisterChat
import com.bot.entity.*
import com.bot.tgapi.Method
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.Semaphore

open class ChatProcessor(val user: User) {
	
	var chat: ChatBuilder = if (user.type == User.Companion.Type.NONAME) RegisterChat(user).getChat()
	else BaseChats.hello(user)
	var message: String? = null
	private val lock = Semaphore(0)
	private var worker: Job
	
	init {
		worker = launch { work() }
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
	fun work() {
		while (true) {
			Optional.of(chat).ifPresent {
				Optional.ofNullable(chat.beforeExecution).ifPresent { it.invoke() }
				try {
									Method.sendMessage(user.id, "Started chat: ${chat.name}")
					chat.actions.forEach {
						sendMessage(it.first)
						lock.acquire()
						if (message == "/home") {
							chat = BaseChats.hello(user)
							return@ifPresent
						}
						try {
							it.second.invoke(message!!)
							if (chat.eachStepAction != null) {
								chat.eachStepAction!!.invoke()
							}
						} catch (e: Exception) {
							println("here!")
							if (chat.errorHandler.first.invoke(e)) {
								throw e
							}
						}
					}
					Optional.ofNullable(chat.onCompleteAction).ifPresent { it.invoke() }
					Optional.ofNullable(chat.onCompleteMessage).ifPresent { sendMessage(it) }
				} catch (e: Exception) {
					val sb = StringBuilder().append(e.javaClass.name).append("  ->  ")
						.append(e.localizedMessage).append("\n")
					e.stackTrace
						.filter { it.className.startsWith("com.bot") }
						.forEach {
							sb.append(it.className).append(" : ").append(it.methodName).append(" @ line ")
								.append(it.lineNumber).append("\n")
						}
					sendMessage(sb.toString())
					e.printStackTrace()
				}
				
				if (chat.nextChatQuestion != null) {
					sendMessage(chat.nextChatQuestion!!)
					lock.acquire()
					if (message == "/home") {
						chat = BaseChats.hello(user)
						return@ifPresent
					}
				}
				if (chat.nextChatDeterminer != null) {
					chat = chat.nextChatDeterminer!!.invoke(message!!)
				} else {
					chat = BaseChats.hello(user)
				}
				
				Optional.ofNullable(chat.afterWorkAction).ifPresent { it.invoke() }
			}
		}
	}
	
	fun input(text: String) {
		message = text
		lock.release(1)
	}
	
	fun sendMessage(response: Response) {
		Method.sendMessage(response.ensureUser(user.id))
	}
	
	fun sendMessage(text: String) {
		Method.sendMessage(user.id, text)
	}
	
}
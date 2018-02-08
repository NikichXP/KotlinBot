package com.bot.logic

import com.bot.chats.BaseChats
import com.bot.chats.RegisterChat
import com.bot.entity.*
import com.bot.tgapi.Method
import com.google.gson.Gson
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.Semaphore

open class ChatProcessor(val user: User) {
	
	var chat: ChatBuilder =
		if (user.type == User.Companion.Type.NONAME) RegisterChat(user).getChat()
		else BaseChats.hello(user)
	var message: String = "/home"
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
	private fun work() {
		while (true) {
			Optional.of(chat).ifPresent {
				chat.beforeExecution?.invoke()
				try {
					var selectedAction: Pair<Response, (String) -> Unit>
					var i = 0
					while (i < chat.actions.size) {
						if (i < 0) {
							i = 0
						}
						
						/*
						
						1. -home, -help, -back
						^ message = ObjectObserver<String>("")
						
						2. -shitty
						while (!selectedAction.over) {
						
						when (message) --- handle home/help/back
						selectedAction.action(message)
						
						}
						
						
						selectedAction = chat.actions[i]
						selectedAction.handle(lock, message)
						
						 */
						selectedAction = chat.actions[i]
						sendMessage(selectedAction.first)
						lock.acquire()
						when (message) {
							"/help", "/home" -> {
								chat = BaseChats.hello(user)
								return@ifPresent
							}
							"/back"          -> {
								i -= 1
							}
							else             -> {
								try {
									selectedAction.second(message)
									chat.eachStepAction?.invoke()
								} catch (e: Exception) {
									println("here!")
									if (chat.errorHandler.first(e)) {
										throw e
									}
								}
								i++
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
					sendMessage(sb.toString())
					e.printStackTrace()
				}
				
				chat.nextChatQuestion?.also {
					sendMessage(it)
					lock.acquire()
					if (message == "/home") {
						chat = BaseChats.hello(user)
						return@ifPresent
					}
				}
				chat = chat.nextChatDeterminer.invoke(message)
				chat.afterWorkAction?.invoke()
			}
		}
	}
	
	fun input(text: String) {
		message = text
		lock.release(1)
	}
	
	private fun sendMessage(response: Response) {
		Method.sendMessage(response.ensureUser(user.id))
	}
	
	private fun sendMessage(text: String) {
		Method.sendMessage(user.id, text)
	}
	
}
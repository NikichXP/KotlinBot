package com.bot.logic.stateprocessors

import com.bot.entity.*
import com.bot.logic.ChatProcessor
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Semaphore

class QuestionableStateProcessor(val user: User, val chatProcessor: ChatProcessor) {
	
	constructor(user: User, chatProcessor: ChatProcessor, chat: QuestionChat) : this(user, chatProcessor) {
		this.chatEntity = chat
	}
	
	val state = State.CUSTOM_CHAT
	lateinit var chatEntity: QuestionChat
	var message: String? = null
	
	private val lock = Semaphore(0)
	private lateinit var worker: CompletableFuture<Void>
	
	fun processChat(chat: QuestionChat) {
		this.chatEntity = chat
		if (this::worker.isInitialized) {
			try {
				worker.cancel(true)
			} catch (e: Exception) {
			}
		}
		worker = CompletableFuture.runAsync { work() }
	}
	
	fun work() {
		chatEntity.actions.forEach {
			chatProcessor.sendMessage(it.first)
			lock.acquire()
			it.second.invoke(message!!)
		}
	}
	
	fun input(text: String) {
		message = text
		lock.release()
	}
	
}
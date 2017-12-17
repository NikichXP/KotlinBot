package com.bot.future

import com.bot.chats.BaseChats
import com.bot.entity.ChatBuilder
import com.bot.entity.Message
import com.bot.entity.Response
import com.bot.entity.User
import com.bot.tgapi.Method

class ChatState(val user: User) {
	
	var chat: ChatBuilder = BaseChats.hello(user)
	var currentQuestion = 0
	
	fun inbox(message: Message) {
		try {
			if (currentQuestion < chat.actions.size) {
				chat.actions[currentQuestion].second.invoke(message.text)
				currentQuestion++
			}
			if (chat.actions.size > currentQuestion) {
				sendMessage(chat.actions[currentQuestion].first)
				return
			}
			
			if (currentQuestion == chat.actions.size) {
				chat.onCompleteAction?.invoke()
				if (chat.onCompleteMessage != null) sendMessage(chat.onCompleteMessage!!)
				
				if (chat.nextChatQuestion != null) {
					sendMessage(chat.nextChatQuestion!!)
					currentQuestion++
					return
				} else {
					currentQuestion++
				}
			}
			
			if (currentQuestion > chat.actions.size) {
				if (chat.nextChatDeterminer != null) {
					chat = chat.nextChatDeterminer!!.invoke(message.text)
				} else {
					chat = BaseChats.hello(user)
				}
				currentQuestion = 0
			}
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
	}
	
	fun sendMessage(response: Response) {
		Method.sendMessage(response.ensureUser(user.id))
	}
	
	fun sendMessage(text: String) {
		Method.sendMessage(user.id, text)
	}
}
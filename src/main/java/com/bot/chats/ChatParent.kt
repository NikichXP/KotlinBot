package com.bot.chats

import com.bot.entity.User
import com.bot.tgapi.Method

open class ChatParent(open var user: User) {
	
	fun sendMessage(text: String) = Method.sendMessage(user.id, text)
	
}
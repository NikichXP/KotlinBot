package com.bot.entity

import com.bot.tgapi.ReplyKeyboardMarkup
import com.google.gson.Gson


data class Response(var chat_id: String, var text: String) {
	
	var reply_markup: ReplyKeyboardMarkup? = null
	
	constructor(user: User, text: String) : this(user.id, text)
	constructor(user: User) : this(user.id, "")
	
	fun withCustomKeyboard (buttons: Array<String>): Response {
		this.reply_markup = ReplyKeyboardMarkup(buttons)
		return this
	}
	
	fun andText(text: String): Response {
		this.text = text
		return this
	}
	
	fun toJson(): String {
//		if (reply_markup != null) {
			return gson.toJson(this)
//		}
//		return gson.toJsonTree(this).asJsonObject.remove("reply_markup").asJsonObject.toString()
	}
	
	companion object {
		val gson = Gson()
	}
}
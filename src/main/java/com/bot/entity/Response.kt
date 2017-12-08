package com.bot.entity

import com.bot.tgapi.ReplyKeyboardMarkup
import com.bot.tgapi.ReplyKeyboardRemove
import com.bot.tgapi.ReplyMarkup
import com.google.gson.Gson
import java.util.*


data class Response(var chat_id: String?, var text: String) {
	
	var reply_markup: ReplyMarkup = ReplyKeyboardRemove()
	
	constructor(user: User, text: String) : this(user.id, text)
	constructor(user: User) : this(user.id, "")
	constructor(chat_id: String?) : this(chat_id, "")
	constructor(text: String, keys: Array<String>) : this(null, text) {
		withCustomKeyboard(keys)
	}
	
	constructor(text: String, keys: Array<Array<String>>) : this(null, text) {
		withMaxtrixKeyboard(keys)
	}
	
	
	fun withCustomKeyboard(buttons: Array<String>): Response {
		this.reply_markup = ReplyKeyboardMarkup(buttons)
		return this
	}
	
	fun withMaxtrixKeyboard(buttons: Array<Array<String>>): Response {
		this.reply_markup = ReplyKeyboardMarkup(buttons)
		return this
	}
	
	fun withText(text: String): Response {
		this.text = text
		return this
	}
	
	fun withViewData(text: String): Response {
		if (text.startsWith("#keyboard")) {
			val keys = LinkedList(text.substring("#keyboard:".length).split("#").toList())
			this.text = keys.poll()
			this.reply_markup = ReplyKeyboardMarkup(keys.toTypedArray())
		} else {
			this.text = text
		}
		return this
	}
	
	fun toJson() = gson.toJson(this)
	
	fun ensureUser(chat_id: String): Response {
		if (this.chat_id == null) this.chat_id = chat_id
		return this
	}
	
	companion object {
		val gson = Gson()
	}
}
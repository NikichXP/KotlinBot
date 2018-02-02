package com.bot.entity

import com.bot.logic.TextResolver
import com.bot.tgapi.InlineKeyboardMarkup
import com.bot.tgapi.ReplyKeyboardMarkup
import com.bot.tgapi.ReplyKeyboardRemove
import com.bot.tgapi.ReplyMarkup
import com.google.gson.Gson
import java.util.*


class Response(var chat_id: String?, text: String) {
	
	var reply_markup: ReplyMarkup = ReplyKeyboardRemove()
	var text: String
	var lateinitFx: (() -> String)? = null
	
	constructor(user: User, text: String) : this(user.id, text)
	constructor(user: User) : this(user.id, "")
	constructor(text: String, keys: Array<String>) : this(null, text) {
		withCustomKeyboard(*keys)
	}
	
	constructor(text: String, keys: Array<Array<String>>) : this(null, text) {
		withCustomKeyboard(keys)
	}
	
	constructor(user: User, text: () -> String) : this(user.id, "") {
		this.lateinitFx = text
	}
	
	constructor(user: String, text: () -> String) : this(user, "") {
		this.lateinitFx = text
	}
	
	constructor(text: () -> String) : this(null, "") {
		this.lateinitFx = text
	}
	
	init {
		this.text = text
	}
	
	fun withCustomKeyboard(vararg buttons: String): Response {
		this.reply_markup = ReplyKeyboardMarkup(buttons as Array<String>)
		return this
	}
	
	fun withCustomKeyboard(buttons: Array<Array<String>>): Response {
		this.reply_markup = ReplyKeyboardMarkup(buttons)
		return this
	}
	
	fun withInlineKeyboard(buttons: Array<String>): Response {
		this.reply_markup = InlineKeyboardMarkup(buttons)
		return this
	}
	
	fun withInlineKeyboard(buttons: Array<Array<String>>): Response {
		this.reply_markup = InlineKeyboardMarkup(buttons)
		return this
	}
	
	fun withText(text: String): Response {
		this.text = text
		return this
	}
	
	fun withLateInitText(text: () -> String): Response {
		this.lateinitFx = text
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
	
	fun toJson(): String {
		if (lateinitFx != null) text = lateinitFx!!.invoke()
		text = TextResolver.getText(text, false)
		return gson.toJson(this)
	}
	
	fun ensureUser(chat_id: String): Response {
		if (this.chat_id == null) this.chat_id = chat_id
		return this
	}
	
	companion object {
		val gson = Gson()
	}
	
	fun findTextSupplements(correct: Boolean) {
		text = TextResolver.getText(text, correct)
	}
}
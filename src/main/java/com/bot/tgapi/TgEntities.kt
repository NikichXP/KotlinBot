package com.bot.tgapi

class ReplyKeyboardMarkup : ReplyMarkup {
	
	constructor(keyboard: ArrayList<ArrayList<KeyboardButton>>) {
		this.keyboard = keyboard
	}
	
	constructor(keys: Array<String>) {
		this.keyboard = ArrayList()
		this.keyboard.add(ArrayList())
		keys.forEach { this.keyboard[0].add(KeyboardButton(it)) }
	}
	
	constructor(buttons: Array<Array<String>>) {
		this.keyboard = ArrayList()
		buttons.forEach {
			val add = ArrayList<KeyboardButton>()
			it.forEach {
				add.add(KeyboardButton(it))
			}
			this.keyboard.add(add)
		}
	}
	
	var keyboard = ArrayList<ArrayList<KeyboardButton>>()
	var resize_keyboard = true
	var one_time_keyboard = false
	
}

class InlineKeyboardMarkup : ReplyMarkup {
	
	constructor(keyboard: ArrayList<ArrayList<InlineKeyboardButton>>) {
		this.inline_keyboard = keyboard
	}
	
	constructor(keys: Array<String>) {
		this.inline_keyboard = ArrayList()
		this.inline_keyboard.add(ArrayList())
		keys.forEach { this.inline_keyboard[0].add(InlineKeyboardButton(it)) }
	}
	
	constructor(buttons: Array<Array<String>>) {
		this.inline_keyboard = ArrayList()
		buttons.forEach {
			val add = ArrayList<InlineKeyboardButton>()
			it.forEach {
				add.add(InlineKeyboardButton(it))
			}
			this.inline_keyboard.add(add)
		}
	}
	
	var inline_keyboard = ArrayList<ArrayList<InlineKeyboardButton>>()
}

class ReplyKeyboardRemove(var remove_keyboard: Boolean = true) : ReplyMarkup

interface ReplyMarkup {

}

class KeyboardButton(var text: String = "") {
}

class InlineKeyboardButton {
	
	var callback_data: String? = null
	var text: String
	
	init {
		text = "%%ERROR: NO TEXT%%"
	}
	
	constructor(text: String) {
		this.text = text
		this.callback_data = text
	}
	
}
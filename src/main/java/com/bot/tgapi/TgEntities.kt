package com.bot.tgapi

class ReplyKeyboardMarkup {
	
	constructor(keyboard: ArrayList<ArrayList<KeyboardButton>>) {
		this.keyboard = keyboard
	}
	
	constructor(keys: Array<String>) {
		this.keyboard = ArrayList()
		this.keyboard.add(ArrayList())
		keys.forEach { this.keyboard[0].add(KeyboardButton(it)) }
	}
	
	var keyboard = ArrayList<ArrayList<KeyboardButton>>()
	var resize_keyboard = true
	var one_time_keyboard = false
}

class KeyboardButton(var text: String = "") {
}
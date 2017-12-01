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
}

class KeyboardButton(var text: String = "") {
}
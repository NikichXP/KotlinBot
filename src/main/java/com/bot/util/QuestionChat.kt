package com.bot.util

import java.util.*

class QuestionChat() {
	
	val actions = LinkedList<Pair<String, (String) -> Unit>>()
	var currentState = Pair(0, 0)
	
	constructor(text: String, action: (String) -> Unit) : this() {
		ask(text, action)
	}
	
	fun ask(text: String, action: (String) -> Unit) {
		actions.add(Pair(text, action))
	}
	
}
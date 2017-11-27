package com.bot.entity


data class Response(var destination: String, var text: String) {
	
	constructor(user: User, text: String) : this(user.id, text)
	constructor(user: User) : this(user.id, "")
	
	fun andText(text: String): Response {
		this.text = text
		return this
	}

}
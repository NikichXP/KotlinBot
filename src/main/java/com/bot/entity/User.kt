package com.bot.entity

import org.springframework.data.annotation.Id

data class User(@Id var id: String,
                var type: Type = Type.CLIENT,
                var fullName: String? = null,
                var username: String? = null,
                var email: String? = null,
                var balance: Double = 0.0) {
	
	constructor(message: Message) : this(
		id = message.senderId,
		fullName = message.firstName ?: "" + " " + message.lastName ?: "",
		username = message.username
	)
	
	companion object {
		enum class Type {
			CLIENT, BROKER, ADMIN, WAIT
		}
	}
}
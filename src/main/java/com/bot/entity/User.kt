package com.bot.entity

import org.springframework.data.annotation.Id

data class User(@Id var id: String,
                var type: Type = Type.CLIENT,
                var balance: Double = 0.0) {
	
	companion object {
		enum class Type {
			CLIENT, BROKER, ADMIN
		}
	}
}
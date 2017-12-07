package com.bot.logic

import com.bot.entity.Message
import com.bot.entity.User
import com.bot.tgapi.Method
import com.bot.repo.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class TelegramInputParser {
	
	@Autowired
	lateinit var userRepo: UserRepo
	
	val chatProcessors = ConcurrentHashMap<String, ChatProcessor>()
	
	fun input(inputJson: String) {
		try {
			val message = Message(inputJson)
			val chatProcessor = chatProcessors.getOrPut(message.senderId, {
				val user = userRepo.findById(message.senderId).orElseGet { userRepo.save(User(message.senderId)) }
				@Suppress("REDUNDANT_ELSE_IN_WHEN")
				return@getOrPut ChatProcessor(user)
			})
			chatProcessor.input(message.text)
		} catch (e: NullPointerException) {
			e.printStackTrace()
			Method.sendMessage("34080460", "error on parse: $inputJson")
		}
		
	}
	
}

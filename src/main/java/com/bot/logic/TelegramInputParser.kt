package com.bot.logic

import com.bot.entity.Message
import com.bot.entity.User
import com.bot.future.AltChatProcessor
import com.bot.tgapi.Method
import com.bot.repo.UserRepo
import com.google.gson.JsonParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.UnsupportedOperationException
import java.util.concurrent.ConcurrentHashMap

@Service
class TelegramInputParser {
	
	@Autowired
	lateinit var userRepo: UserRepo
	
	val chatProcessors = ConcurrentHashMap<String, ChatProcessor>()
	@Autowired
	lateinit var newChatProcessor: AltChatProcessor
	
	fun input(inputJson: String) {
		
		println("Input JSON:" + inputJson)
		
		try {
			var message: Message
			var jsonObject = JsonParser().parse(inputJson).asJsonObject
			when {
				jsonObject.has("message")        -> message = Message(inputJson)
				jsonObject.has("callback_query") -> {
					jsonObject = jsonObject.getAsJsonObject("callback_query")
					message = Message(
						id = jsonObject.get("id").asString,
						senderId = jsonObject.getAsJsonObject("from").get("id").asString,
						text = jsonObject["data"].asString,
						textMessageJson = jsonObject
					)
				}
				else                             -> throw UnsupportedOperationException("Need to update Telegram Input Parser, $inputJson")
			}
			chatProcessors.getOrPut(message.senderId, {
				val user = userRepo.findById(message.senderId).orElseGet { userRepo.save(User(message)) }
				@Suppress("REDUNDANT_ELSE_IN_WHEN")
				return@getOrPut ChatProcessor(user)
			}).input(message.text)
			//			newChatProcessor.inbox(message)
		} catch (e: Exception) {
			e.printStackTrace()
			Method.sendMessage("34080460", "error on parse: $inputJson")
		}
		
	}
	
}

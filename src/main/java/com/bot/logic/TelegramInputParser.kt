package com.bot.logic

import com.bot.entity.Message
import com.bot.repo.UserFactory
import com.bot.tgapi.Method
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import org.springframework.stereotype.Service
import java.lang.UnsupportedOperationException
import java.util.concurrent.ConcurrentHashMap

@Service
class TelegramInputParser {
	
	val chatProcessors = ConcurrentHashMap<String, ChatProcessor>()
	
	fun input(inputJson: String) {
		
		println("Input JSON:" + inputJson)
		
		try {
			val message: Message
			var jsonObject: JsonObject
			try {
				jsonObject = JsonParser().parse(inputJson).asJsonObject
			} catch (e: JsonSyntaxException) {
				return
			}
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
				val user = UserFactory[message]
				return@getOrPut ChatProcessor(user)
			}).input(message.text)
		} catch (e: Exception) {
			e.printStackTrace()
			Method.sendMessage("34080460", "error on parse: $inputJson")
		}
		
	}
	
}

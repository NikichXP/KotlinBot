package com.bot.entity

import com.bot.tgapi.Method
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import lombok.Data
import org.springframework.data.annotation.Id

@Data
data class Message(@Id val id: String,
                   var text: String? = null,
                   var document: Pair<String, String>? = null,
                   var senderId: String,
                   var firstName: String? = null,
                   var lastName: String? = null,
                   var username: String? = null,
                   var textMessageJson: JsonObject) {
	
	constructor(jsonObject: JsonObject) : this(
		id = jsonObject.getAsJsonObject("chat").get("id").asString + "_" + jsonObject.get("message_id"),
		senderId = jsonObject.getAsJsonObject("from").get("id").asString,
		textMessageJson = jsonObject
	) {
		try {
			firstName = jsonObject.getAsJsonObject("from").get("first_name").asString
		} catch (e: Exception) {
		}
		try {
			lastName = jsonObject.getAsJsonObject("from").get("last_name").asString
		} catch (e: Exception) {
		}
		try {
			username = jsonObject.getAsJsonObject("from").get("username").asString
		} catch (e: Exception) {
		}
		if (jsonObject.has("text")) {
			text = jsonObject.get("text").asString
		}
		if (jsonObject.has("document")) {
			document = jsonObject.getAsJsonObject("document")
				.let { it.get("file_id").asString to it.get("file_name").asString }
		}
		if (listOf("photo", "sticker", "audio").stream().anyMatch { jsonObject.has(it) }) {
			Method.sendMessage(senderId, "Stickers, music, photos (not as document) and audio messages aren't supported.")
			throw IllegalArgumentException("These type of data not available right now.")
		}
	}
	
	constructor(json: String) : this(JsonParser().parse(json).asJsonObject.getAsJsonObject("message"))
	
	
}

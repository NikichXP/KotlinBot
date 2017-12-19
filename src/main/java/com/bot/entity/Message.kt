package com.bot.entity

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import lombok.Data
import org.springframework.data.annotation.Id

@Data
data class Message(@Id val id: String,
                   var text: String,
                   var senderId: String,
                   var firstName: String? = null,
                   var lastName: String? = null,
                   var username: String? = null,
                   var textMessageJson: JsonObject) {
	
	constructor(textMessageJson: JsonObject) : this(
		text = textMessageJson.get("text").asString,
		id = textMessageJson.getAsJsonObject("chat").get("id").asString + "_" + textMessageJson.get("message_id"),
		senderId = textMessageJson.getAsJsonObject("from").get("id").asString,
		textMessageJson = textMessageJson
	) {
		try {
			firstName = textMessageJson.getAsJsonObject("from").get("first_name").asString
		} catch (e: Exception){}
		try {
			lastName = textMessageJson.getAsJsonObject("from").get("last_name").asString
		} catch (e: Exception){}
		try {
			username = textMessageJson.getAsJsonObject("from").get("username").asString
		} catch (e: Exception){}
	}
	
	constructor(json: String) : this(JsonParser().parse(json).asJsonObject.getAsJsonObject("message"))
	
}

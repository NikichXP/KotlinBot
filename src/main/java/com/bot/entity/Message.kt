package com.bot.entity

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import lombok.Data
import org.springframework.data.annotation.Id

@Data
data class Message(@Id val id: String,
                   var text: String,
                   var senderId: String,
                   var textMessageJson: JsonObject) {
	
	constructor(textMessageJson: JsonObject) : this(
		text = textMessageJson.get("text").asString,
		id = textMessageJson.getAsJsonObject("chat").get("id").asString + "_" + textMessageJson.get("message_id"),
		senderId = textMessageJson.getAsJsonObject("from").get("id").asString,
		textMessageJson = textMessageJson
	)
	
	constructor(json: String) : this(JsonParser().parse(json).asJsonObject.getAsJsonObject("message"))
	
}

package com.bot.methods

import com.bot.entity.Response
import com.google.gson.Gson
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType


object Method {
	
	val botToken = "459040479:AAEy_zLBpoDBh0B3EccUy00kHjzSGQRr99M"
	val hostName = "https://b79ebba9.ngrok.io/"
	val baseURL = "https://api.telegram.org/bot$botToken/"
	
	val restTemplate = RestTemplate()
	val gson = Gson()
	
	init {
		restTemplate.errorHandler = object : ResponseErrorHandler {
			override fun hasError(response: ClientHttpResponse?) = false
			override fun handleError(response: ClientHttpResponse?) = Unit
		}
	}
	
	fun getMe() {
	
	}
	
	fun sendMessage(chatId: String, text: String) {
		method("sendMessage", "chat_id", chatId, "text", text)
	}
	
	fun sendMessage(response: Response) = sendMessage(response.destination, response.text)
	
	fun setupWebhook() {
		method("setWebhook", "url", hostName)
	}
	
	fun method(name: String, vararg paramsArr: String) {
		val params = HashMap<String, String>()
		(0 until (paramsArr.size / 2)).forEach {
			params.put(paramsArr[it * 2],
				paramsArr[it * 2 + 1])
		}
		
		val headers = HttpHeaders()
		headers.contentType = MediaType.APPLICATION_JSON
		
		val entity = HttpEntity<String>(gson.toJson(params), headers)
		restTemplate.postForObject(baseURL + name, entity, String::class.java)
	}
	
	
}

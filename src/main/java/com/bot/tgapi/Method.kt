package com.bot.tgapi

import com.bot.entity.Message
import com.bot.entity.Response
import com.bot.logic.TextResolver
import com.google.gson.Gson
import com.nikichxp.util.Json
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.charset.Charset


object Method {
	
	val botToken = "459040479:AAEy_zLBpoDBh0B3EccUy00kHjzSGQRr99M"
	val hostName = "https://68e708d2.ngrok.io"
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
		method("sendMessage", "chat_id", chatId, "text", TextResolver.getText(text, false))
	}
	
	
	fun sendMessageWithKeyboard(message: Message) {
		method("sendMessage", gson.toJson(message))
	}
	
	fun sendMessage(response: Response) {
		response.findTextSupplements(false)
		val res = response.toJson()
		println(res)
		method("sendMessage", res)
	}
	
	fun setupWebhook() {
		method("setWebhook", "url", hostName)
	}
	
	fun method(name: String, vararg paramsArr: String) {
		val params = HashMap<String, String>()
		(0 until (paramsArr.size / 2)).forEach {
			params.put(paramsArr[it * 2],
				paramsArr[it * 2 + 1])
		}
		
		method(name, gson.toJson(params))
	}
	
	fun method(name: String, request: Json) {
		method(name, request.json())
	}
	
	fun method(name: String, requestBody: String) {
		val headers = HttpHeaders()
		headers.contentType = MediaType.APPLICATION_JSON
		headers.add("charset", "utf-8")
		
		val entity = HttpEntity(requestBody.toByteArray(Charset.forName("UTF-8")), headers)
		restTemplate.postForObject(baseURL + name, entity, String::class.java)
	}
	
}

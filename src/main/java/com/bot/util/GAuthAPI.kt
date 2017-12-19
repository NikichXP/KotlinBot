package com.bot.util

import com.nikichxp.util.JsonUtil
import com.nikichxp.util.Ret
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

import javax.servlet.http.HttpServletResponse
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

import com.nikichxp.util.Async.async

@RestController
@RequestMapping("/api/google/auth")
class GAuthAPI @Autowired
constructor(context: ApplicationContext) {
	
	private val redirect_uri = "https://avant-html.herokuapp.com/test/echo"
	private var client_id: String? = null
	private var client_secret: String? = null
	private var refresh_token: String? = null
	var accessToken: String? = null //gets after stage #3. if null - cannot write to Gdisk
	
	init {
		async {
			client_id = context.environment.getProperty("google.client.id")
			client_secret = System.getenv("google_client_secret")
			if (client_secret == null) {
				try {
					client_secret = BufferedReader(
						InputStreamReader(
							FileInputStream("C:/google_client_secret.dat"))
					).readLine()
				} catch (ignored: Exception) {
				}
				
			}
			try {
				refresh_token = "1/IdTnDqD3pCkwBOcIptek6uIco3-FwTaUYKAlwlzLmF8"
				updateAccessToken()
			} catch (e: Exception) {
				println("Cannot update token. Need re-auth.")
			}
			
			println("GSheets service started, " + (refresh_token != null) + " & " + (this.accessToken != null))
		}
	}
	
	@GetMapping("/status")
	fun status(): ResponseEntity<*> {
		return if (this.accessToken == null)
			Ret.code(401, "Failure")
		else if (refresh_token == null) Ret.code(401, "refresh failure") else Ret.ok("OK")
	}
	
	@GetMapping
	fun auth(@RequestParam(value = "redirect", required = false) redirect: Boolean?,
	         resp: HttpServletResponse) {
		if (redirect != null && redirect) {
			resp.sendRedirect("https://accounts.google.com/o/oauth2/v2/auth?redirect_uri=" +
				redirect_uri +
				"&response_type=" +
				RESPONSE_TYPE +
				"&client_id=" +
				client_id +
				"&scope=" +
				SCOPE +
				"&access_type=" +
				ACCESS_TYPE +
				"&prompt=" +
				PROMPT)
		}
		resp.writer.write("https://accounts.google.com/o/oauth2/v2/auth?redirect_uri=" +
			redirect_uri +
			"&response_type=" +
			RESPONSE_TYPE +
			"&client_id=" +
			client_id +
			"&scope=" +
			SCOPE +
			"&access_type=" +
			ACCESS_TYPE +
			"&prompt=" +
			PROMPT)
	}
	
	@GetMapping("/proceed")
	@Throws(IOException::class)
	fun proceed(@RequestParam("code") code: String): ResponseEntity<*> {
		
		val headers = HttpHeaders()
		headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
		
		val map = LinkedMultiValueMap<String, String>()
		map.add("code", code)
		map.add("client_id", client_id)
		map.add("client_secret", client_secret)
		map.add("grant_type", GRANT_TYPE_1)
		map.add("redirect_uri", redirect_uri)
		val request = HttpEntity<MultiValueMap<String, String>>(map, headers)
		
		val response = restTemplate.postForEntity("https://www.googleapis.com/oauth2/v4/token", request, String::class.java)
		println(response.body)
		
		if (response.statusCodeValue == 200) {
			refresh_token = JsonUtil.of(response.body!!).getX("refresh_token")
		}
		
		if (refresh_token != null) {
			async { this.updateAccessToken() }
		}
		return ResponseEntity.ok(response.body!!)
	}
	
	@Scheduled(cron = "0 0 * * * *")
	private fun updateAccessToken() {
		
		println("Update token")
		if (refresh_token == null) {
			println("refresh_token is null")
			return
		}
		
		val headers = HttpHeaders()
		headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
		
		val map = LinkedMultiValueMap<String, String>()
		map.add("refresh_token", refresh_token)
		map.add("client_id", client_id)
		map.add("client_secret", client_secret)
		map.add("grant_type", GRANT_TYPE_2)
		val request = HttpEntity<MultiValueMap<String, String>>(map, headers)
		
		val response = restTemplate.postForEntity("https://www.googleapis.com/oauth2/v4/token", request, String::class.java)
		try {
			this.accessToken = JsonUtil.of(response.body!!).getX("access_token")
		} catch (e: Exception) {
			println(response.body)
		}
	}
	
	companion object {
		
		private val SCOPE = "https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive.file+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive.readonly+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fspreadsheets+" +
			"https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fspreadsheets.readonly"
		
		private val ACCESS_TYPE = "offline"
		private val PROMPT = "consent"
		private val RESPONSE_TYPE = "code"
		private val GRANT_TYPE_1 = "authorization_code"
		private val GRANT_TYPE_2 = "refresh_token"
		private val restTemplate = RestTemplate()
		
		init {
			restTemplate.errorHandler = object : ResponseErrorHandler {
				@Throws(IOException::class)
				override fun hasError(clientHttpResponse: ClientHttpResponse): Boolean {
					return false
				}
				
				@Throws(IOException::class)
				override fun handleError(clientHttpResponse: ClientHttpResponse) {
				}
			}
		}
	}
}

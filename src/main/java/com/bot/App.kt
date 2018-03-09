package com.bot

import com.bot.repo.UserFactory
import com.bot.tgapi.Method
import com.bot.util.*
import org.apache.http.impl.client.HttpClients
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

@EnableScheduling
@SpringBootApplication
open class App

object Ctx {
	val lock = Object()
	val AUTHORS_TELEGRAMS = arrayOf("34080460", "80523220")
	var ctx: ConfigurableApplicationContext? = null
	
	operator fun <T> get(clazz: Class<T>): T {
		synchronized(lock) {
			return ctx!!.getBean(clazz)
		}
	}
	
	fun variable(name: String): String {
		synchronized(lock) {
			return Optional.ofNullable(ctx!!.environment.getProperty(name)).orElse(System.getenv(name))!!
		}
	}
}

fun main(args: Array<String>) {
	synchronized(Ctx.lock) {
		Ctx.ctx = SpringApplication.run(App::class.java)
	}
	Logger.getLogger("AppLoader").log(Level.INFO, "Loading complete")
	UserFactory
	Method.setupWebhook()
}

@Component
class Scheduler {
	
	final val restTemplate: RestTemplate
	
	init {
		val requestFactory = HttpComponentsClientHttpRequestFactory(HttpClients.createDefault())
		restTemplate = RestTemplate(requestFactory)
		restTemplate.errorHandler = object : ResponseErrorHandler {
			override fun hasError(clientHttpResponse: ClientHttpResponse): Boolean {
				return false
			}
			
			override fun handleError(clientHttpResponse: ClientHttpResponse) {
				if (clientHttpResponse.rawStatusCode == 403) {
					println("Warning! Illegal permissions!")
				}
			}
		}
	}
	
	@Scheduled(cron = "0 * * * * *")
	fun pingOtherBots() {
		restTemplate.getForObject<String>("https://checkmyqueu.herokuapp.com/ping")
	}
}
package com.bot

import com.bot.tgapi.Method
import com.bot.util.*
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

@SpringBootApplication
open class App

object Ctx {
	val lock = Object()
	val AUTHORS_TELEGRAMS = arrayOf("34080460")
	var ctx: ConfigurableApplicationContext? = null
	
	fun <T> get(clazz: Class<T>): T {
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
	Method.setupWebhook()
	
	//	val x: Double = 10_000_000.02
	//
	//	println(DecimalFormat("#,###.##").format(x))
	//	val a = 155L
	
	println(LocalDateTime.now().plusDays(1).plusHours(1).plusMinutes(15).getTimeDiff(LocalDateTime.now()))
}
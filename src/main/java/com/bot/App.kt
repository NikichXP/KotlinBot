package com.bot

import com.bot.logic.TextResolver
import com.bot.tgapi.Method
import com.google.gson.Gson
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.HashMap

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
	Logger.getGlobal().log(Level.INFO, "Loading complete")
	Method.setupWebhook()
	
//	val map = Gson().fromJson<ConcurrentHashMap<String, String>>("""{"key1":"value1","key2":"value2"}""", ConcurrentHashMap::class.java)
//	map.put("key1", "value1")
//	map.put("key2", "value2")
	
//	TextResolver.foo()
//
//	println(map)
	
//	println(Gson().toJson(map).toString())
}
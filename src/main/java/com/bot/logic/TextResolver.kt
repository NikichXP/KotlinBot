package com.bot.logic

import com.google.gson.Gson
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object TextResolver {
	
	/**
	 * Json, set with texts - their translations: strings
	 */
	private val storedData: ConcurrentHashMap<String, String>
	/**
	 * Stored main menu, optimization
	 */
	val mainMenu: Array<Array<String>>
	
	init {
		storedData = Gson().fromJson<ConcurrentHashMap<String, String>>(BufferedReader(
			InputStreamReader(
				FileInputStream(System.getProperty("user.dir") + "\\texts.jsonx")
			)
		).lines().filter { !it.trim().startsWith("//") }.reduce { a, b -> a + b }.orElse("{}"), ConcurrentHashMap::class.java)
		
		storedData.forEach { t: String, u: String ->
			storedData.put(t.toLowerCase(), u)
		}
		
		val menuString = storedData["menuComponents"]
		
		if (menuString != null) {
			mainMenu = menuString.split("#").stream().map {
				// need to get an array
				return@map it.split(",").stream().map {
					return@map if (it.startsWith("$")) {
						if (storedData[it] != null) {
							storedData[it]!!
						} else it
					} else it
				}.toArray<String>({ length -> arrayOfNulls(length) })
			}.toArray<Array<String>>({ length -> arrayOfNulls(length) })
		} else {
			mainMenu = arrayOf()
		}
		
		println(Arrays.toString(mainMenu[0]))
		println(storedData)
	}
	
	fun foo() = "bar"
	
	fun getText(text: String, correct: Boolean = false): String = storedData[text.toLowerCase()]
		?: storedData["$" + text.toLowerCase()]
		?: if (correct) "[[Add text here: $text]]" else text
	
	fun getCausedVar(text: String) =
		storedData.filterKeys { storedData[it] == text }.keys.stream().findAny().orElse(null)
	
}
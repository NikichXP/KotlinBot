package com.bot.logic

import com.bot.entity.State
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
	/**
	 * States we have. Better to be in another class. Every value is stored with $ and without it.
	 */
	val statesStrings: ConcurrentHashMap<String, State>
	
	init {
		storedData = Gson().fromJson<ConcurrentHashMap<String, String>>(BufferedReader(
			InputStreamReader(
				FileInputStream(System.getProperty("user.dir") + "\\texts.json")
			)
		).lines().reduce { a, b -> a + b }.orElse("{}"), ConcurrentHashMap::class.java)
		
		val menuString = storedData["menu_components"]
		
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
		
		statesStrings = ConcurrentHashMap()
		State.values().forEach {
			statesStrings.put("$" + it.value, it)
			statesStrings.put(it.value, it)
		}
		
		
		println(Arrays.toString(mainMenu[0]))
		println(storedData)
	}
	
	fun foo() = "bar"
	
	fun getStateText(state: State) = storedData["state.${state.value.toLowerCase()}"] ?: "[[Add merge text: 'state.${state.value.toLowerCase()}']]"
	fun getText(text: String) = storedData[text.toLowerCase()] ?: storedData["$" + text.toLowerCase()] ?: "[[Add text here: $text]]"
	
	fun getCausedVar(text: String) =
		storedData.filterKeys { storedData[it] == text }.keys.stream().findAny().orElse(null)
	
	fun getResultStateByText(text: String): State {
		val stateName = getCausedVar(text) ?: return State.HELLO
		return statesStrings[stateName.toUpperCase()] ?: State.HELLO
	}
	
}
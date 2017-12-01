package com.bot.logic.dialogprocessor

import com.bot.entity.Response
import com.bot.entity.User
import com.bot.logic.TextResolver

class ClientDialogProcessor(val user: User) : DialogProcessor {
	
	var state = State.HELLO
	var microState = 0
	
	override fun input(text: String): Response {
		
		when (text) {
			"/home", "/help" -> return result(State.HELLO)
			"/test" -> return Response(user, "TEST123").withCustomKeyboard(arrayOf("text1", "2", "three"))
			
		// more options here
		}
		
		val response = Response(user)
		return when (state) {
			State.HELLO ->
				when (text) {
					"/info"      -> response.andText("INFO")
					"/setData"   -> result(State.PROMPT_NAME)
					"/customers" -> result(State.CUSTOMERS)
					else         -> response.andText(state.value)
				}
			
			else        -> response.andText("elsebranch")
		}
	}
	
	/**
	 * Render with pre-defined text
	 */
	fun result(newState: State): Response {
		this.state = newState
		return Response(user, TextResolver.getText(newState.value))
	}
	
	/**
	 * Render with custom text
	 */
	fun result(newState: State, text: String): Response {
		this.state = newState
		return Response(user, text)
	}
	
	enum class State(val value: String) {
		HELLO("HELLO"),
		PROMPT_NAME("PROMPT_NAME"), PROMPT_ADDRESS("PROMPT_ADDRESS"), PROMPT_PHONE("PROMPT_PHONE"),
		CUSTOMERS("CUSTOMERS")
		
	}
	
}

/**
 * Hello phase: "this bot can ......, /info, /setData
 *
 */
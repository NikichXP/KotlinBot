package com.bot.logic.dialogprocessor

import com.bot.entity.Response
import com.bot.entity.User

class ClientDialogProcessor(val user: User) : DialogProcessor {
	
	var state: State = State.HELLO
	
	override fun input(text: String): Response {
		val response = Response(user)
		return when (state) {
			State.HELLO ->
				when (text) {
					"/info"    -> response.andText("INFO")
					"/setData" -> result(State.PROMPT_NAME)
					else       -> response.andText(state.value)
				}
			
			else                                                            -> response.andText("elsebranch")
		}
	}
	
	fun result(newState: State): Response {
		this.state = newState
		return Response(user, newState.value)
	}
	
	fun result(newState: State, text: String): Response {
		this.state = newState
		return Response(user, text)
	}
	
	override fun getResponse(): Response {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	
	enum class State(val value: String) {
		HELLO("HELLO"),
		PROMPT_NAME("PROMPT_NAME"), PROMPT_ADDRESS("PROMPT_ADDRESS"), PROMPT_PHONE("PROMPT_PHONE"),
		
	}
	
}

/**
 * Hello phase: "this bot can ......, /info, /setData
 *
 */
package com.bot.logic

import com.bot.entity.Response
import com.bot.entity.User

open class DialogProcessor(val user: User) {
	
	var state = State.HELLO
	var microState = 0
	
	fun input(text: String): Response {
		
		println(TextResolver.getResultStateByText(text))
		
		when (text) {
			"/home", "/help" -> return withResult(State.HELLO, Response(user, "Look at keyboard").withMaxtrixKeyboard(TextResolver.mainMenu))
			"/test"          -> return Response(user, "TEST123").withCustomKeyboard(arrayOf("text1", "2", "three"))
		
		// more options here
		}
		
		val response = Response(user)
		return when (state) {
			State.HELLO -> route(State.HELLO, text)
			
			else        -> response.andText("elsebranch")
		}
	}
	
	private fun route(state: State, text: String): Response =
		when (state) {
			State.HELLO -> result(TextResolver.getResultStateByText(text))
			else        -> result(State.HELLO)
		}
	
	fun withResult(newState: State, response: Response): Response {
		this.state = newState
		return response
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
		CUSTOMERS("CUSTOMERS"),
		CREATE_CUSTOMER("CREATE_CUSTOMER"), CREATE_REQUEST("CREATE_REQUEST"), MY_REQUESTS("MY_REQUESTS")
		
	}
	
}
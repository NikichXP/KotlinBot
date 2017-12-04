package com.bot.logic

import com.bot.entity.*

open class DialogProcessor(val user: User) {
	
	var state = State.HELLO
	var stateProcessor = StateProcessorFactory.getByState(State.HELLO, user, this)
	
	fun input(text: String): Response {
		
		when (text) {
			"/home", "/help" -> return withResult(State.HELLO, Response(user, "Look at keyboard").withMaxtrixKeyboard(TextResolver.mainMenu))
			"/test"          -> return Response(user, "TEST123").withCustomKeyboard(arrayOf("text1", "2", "three"))
		}
		
		println(state)
		
		if (state != stateProcessor.state) {
			stateProcessor = StateProcessorFactory.getByState(state, user, this)
		}
		
		val responseBlock = stateProcessor.input(text)
		this.state = responseBlock.state
		return responseBlock.response
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
	
	
}
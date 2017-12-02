package com.bot.logic

import com.bot.entity.*

open class DialogProcessor(val user: User) {
	
	var state = State.HELLO
	var stateProcessor = StateProcessorFactory.getByState(State.HELLO, user)
	
	fun input(text: String): Response {
		
		when (text) {
			"/home", "/help" -> return withResult(State.HELLO, Response(user, "Look at keyboard").withMaxtrixKeyboard(TextResolver.mainMenu))
			"/test"          -> return Response(user, "TEST123").withCustomKeyboard(arrayOf("text1", "2", "three"))
		}
		
		println(state)
		
		if (state != stateProcessor.state) {
			stateProcessor = StateProcessorFactory.getByState(state, user)
		}
		
		val responseBlock = stateProcessor.input(text)
		this.state = responseBlock.state
		return responseBlock.response
		
		//		val response = Response(user)
		//		return when (state) {
		//			State.HELLO -> route(State.HELLO, text)
		////			State.CREATE_CUSTOMER -> DialogStateProcessor.createCustomer(text, state, microState, user)
		//			else        -> response.andText("elsebranch")
		//		}
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
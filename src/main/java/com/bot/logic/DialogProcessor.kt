package com.bot.logic

import com.bot.entity.*
import com.bot.logic.stateprocessors.QuestionableStateProcessor
import com.bot.logic.stateprocessors.StateProcessor

open class DialogProcessor(val user: User) {
	
	var state = State.HELLO
	var stateProcessor = StateProcessorFactory.getByState(State.HELLO, user, this)
	var isProcessorIntercepted = false
	
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
		
		if (isProcessorIntercepted) {
			isProcessorIntercepted = false
			try {
				responseBlock.response.text = (stateProcessor as QuestionableStateProcessor).start()
				return responseBlock.response
			} catch (e: Exception) {
				e.printStackTrace()
				return input(text)
			}
		} else {
			this.state = responseBlock.state
			return responseBlock.response
		}
	}
	
	fun interceptHandle(stateProcessor: StateProcessor) {
		this.state = stateProcessor.state
		this.stateProcessor = stateProcessor
		this.isProcessorIntercepted = true
	}
	
	fun withResult(newState: State, response: Response): Response {
		this.state = newState
		return response
	}
	
}
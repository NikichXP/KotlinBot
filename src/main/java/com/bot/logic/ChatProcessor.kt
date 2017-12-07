package com.bot.logic

import com.bot.entity.*
import com.bot.logic.stateprocessors.QuestionableStateProcessor
import com.bot.logic.stateprocessors.StateProcessor
import com.bot.tgapi.Method

open class ChatProcessor(val user: User) {
	
	var state = State.HELLO
	var stateProcessor = StateProcessorFactory.getByState(State.HELLO, user, this)
	var isProcessorIntercepted = false
	lateinit var interceptedDialog: QuestionableStateProcessor
	
	fun input(text: String) {
		
		when (text) {
			"/home", "/help" -> {
				sendMessage(withResult(State.HELLO, Response(user, "Look at keyboard").withMaxtrixKeyboard(TextResolver.mainMenu)))
				return
			}
			"/test"          -> {
				sendMessage(Response(user, "TEST123").withCustomKeyboard(arrayOf("text1", "2", "three")))
				return
			}
		}
		
		if (isProcessorIntercepted) {
			interceptedDialog.input(text)
			return
		}
		
		println(state)
		
		if (state != stateProcessor.state) {
			stateProcessor = StateProcessorFactory.getByState(state, user, this)
		}
		
		val responseBlock = stateProcessor.input(text)
		state = responseBlock.state
		sendMessage(responseBlock.response)
	}
	
	fun sendMessage(response: Response) {
		Method.sendMessage(response)
	}
	
	fun sendMessage(text: String) {
		Method.sendMessage(user.id, text)
	}
	
	fun interceptHandle(stateProcessor: QuestionableStateProcessor) {
		this.state = stateProcessor.state
		this.interceptedDialog = stateProcessor
		this.isProcessorIntercepted = true
	}
	
	fun cancelInterception(state: State) {
		this.state = state
		this.isProcessorIntercepted = false
	}
	
	fun withResult(newState: State, response: Response): Response {
		this.state = newState
		return response
	}
	
}
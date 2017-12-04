package com.bot.logic.stateprocessors

import com.bot.entity.Response
import com.bot.entity.ResponseBlock
import com.bot.entity.State
import com.bot.entity.User
import com.bot.util.QuestionChat

class QuestionableStateProcessor(override val user: User, val parentStateProcessor: StateProcessor,
                                 val questions: QuestionChat) : StateProcessor {
	
	lateinit var nextAction: (String) -> Unit
	lateinit var message: String
	
	override val state = State.CUSTOM_CHAT
	
	fun start(): String {
		val firstPair = questions.next()
		nextAction = firstPair.second
		return firstPair.first
	}
	
	override fun input(text: String): ResponseBlock {
		
		if (this::nextAction.isInitialized) {
			nextAction.invoke(text)
		}
		
		if (questions.isCompleted) {
			questions.afterAll.invoke()
			// set handler to previous text processor
		}
		
		try {
			
			val next = questions.next()
			
			message = next.first
			nextAction = next.second
		} catch (e: Exception) {
			message = questions.
		}
		
		return ResponseBlock(Response(user, message), state)
	}
}
package com.bot.logic.stateprocessors

import com.bot.entity.ResponseBlock
import com.bot.entity.State
import com.bot.entity.User
import com.bot.util.QuestionChat

class QuestionableStateProcessor(override val user: User, val parentStateProcessor: StateProcessor,
                                 val questions: QuestionChat) : StateProcessor {
	
	lateinit var nextAction: (String) -> Unit
	
	override val state = State.CUSTOM_CHAT
	
	override fun input(text: String): ResponseBlock {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
}